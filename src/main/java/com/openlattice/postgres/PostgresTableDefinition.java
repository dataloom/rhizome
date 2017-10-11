/*
 * Copyright (C) 2017. OpenLattice, Inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * You can contact the owner of the copyright at support@openlattice.com
 *
 */

package com.openlattice.postgres;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.hazelcast.util.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class PostgresTableDefinition implements TableDefinition {
    private static final Logger logger = LoggerFactory.getLogger( PostgresTableDefinition.class );

    private final String name;
    private final LinkedHashSet<PostgresColumnDefinition> primaryKey = new LinkedHashSet<>();
    private final LinkedHashSet<PostgresColumnDefinition> columns    = new LinkedHashSet<>();
    private final LinkedHashSet<PostgresColumnDefinition> unique     = new LinkedHashSet<>();
    private final LinkedHashSet<PostgresIndexDefinition>  indexes    = new LinkedHashSet<>();

    private final Map<String, PostgresColumnDefinition> columnMap = Maps.newHashMap();

    private boolean ifNotExists = true;

    public PostgresTableDefinition( String name ) {
        this.name = name;
    }

    public PostgresTableDefinition addColumns( PostgresColumnDefinition... columnsToAdd ) {
        List<PostgresColumnDefinition> colList = Arrays.asList( columnsToAdd );
        colList.stream().forEach( col -> columnMap.put( col.getName(), col ) );
        this.columns.addAll( colList );
        return this;
    }

    public PostgresTableDefinition addIndexes( PostgresIndexDefinition... indexes ) {
        this.indexes.addAll( Arrays.asList( indexes ) );
        return this;
    }

    public String getName() {
        return name;
    }

    public LinkedHashSet<PostgresColumnDefinition> getPrimaryKey() {
        return primaryKey;
    }

    public PostgresTableDefinition primaryKey( PostgresColumnDefinition... primaryKeyColumns ) {
        checkNotNull( primaryKeyColumns, "Cannot set null primary key" );
        /*
         * Technically this will allow you to set several empty primary keys which are all equivalent.
         * This will allow
         */
        checkState( primaryKey.isEmpty(), "Primary key has already been set." );
        primaryKey.addAll( Arrays.asList( primaryKeyColumns ) );
        return this;
    }

    public LinkedHashSet<PostgresColumnDefinition> getColumns() {
        return columns;
    }

    public LinkedHashSet<PostgresColumnDefinition> getUnique() {
        return unique;
    }

    public PostgresTableDefinition setUnique( PostgresColumnDefinition... uniqueColumns ) {
        checkNotNull( uniqueColumns, "Cannot set null unique columns" );
        /*
         * Technically this will allow you to set several empty primary keys which are all equivalent.
         * This will allow
         */
        checkState( unique.isEmpty(), "Primary key has already been set." );
        unique.addAll( Arrays.asList( uniqueColumns ) );
        return this;
    }

    public LinkedHashSet<PostgresIndexDefinition> getIndexes() {
        return indexes;
    }

    public PostgresColumnDefinition getColumn( String name ) {
        return columnMap.get( name );
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    @Override
    public String createTableQuery() {
        validate();
        StringBuilder ctb = new StringBuilder( "CREATE TABLE " );
        if ( ifNotExists ) {
            ctb.append( " IF NOT EXISTS " );
        }

        String columnSql = columns.stream()
                .map( PostgresColumnDefinition::sql )
                .collect( Collectors.joining( "," ) );

        ctb.append( name ).append( " (" ).append( columnSql );

        if ( !primaryKey.isEmpty() ) {
            String pkSql = primaryKey.stream()
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.joining( ", " ) );
            ctb.append( ", PRIMARY KEY (" ).append( pkSql ).append( " )" );

        }

        if ( !unique.isEmpty() ) {
            String uSql = unique.stream()
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.joining( "," ) );
            ctb.append( ", UNIQUE (" ).append( uSql ).append( " )" );

        }

        ctb.append( ")" );
        return ctb.toString();
    }

    public String insertQuery( PostgresColumnDefinition... requestedColumns ) {
        return insertQuery( Optional.empty(), Arrays.asList( requestedColumns ) );
    }

    public String insertQuery( Optional<String> onConflict, List<PostgresColumnDefinition> requestedColumns ) {
        if ( this.columns.containsAll( requestedColumns ) ) {
            StringBuilder insertSql = new StringBuilder( "INSERT INTO " ).append( name );
            if ( !requestedColumns.isEmpty() ) {
                insertSql.append( " (" )
                        .append( requestedColumns.stream().map( PostgresColumnDefinition::getName )
                                .collect( Collectors.joining( "," ) ) )
                        .append( ") VALUES (" )
                        .append( StringUtils.repeat( "?", ", ", requestedColumns.size() ) )
                        .append( ") " );
            } else {
                //All columns
                insertSql.append( " (" )
                        .append( StringUtils.repeat( "?", ", ", columns.size() ) )
                        .append( ") " );
            }

            onConflict.ifPresent( insertSql::append );

            return insertSql.toString();
        } else {
            List<String> missingColumns = requestedColumns.stream()
                    .filter( c -> !this.columns.contains( c ) )
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.toList() );
            String errMsg = "Table is missing requested columns: " + String.valueOf( missingColumns );
            logger.error( errMsg );
            throw new IllegalArgumentException( errMsg );
        }
    }

    public String updateQuery(
            List<PostgresColumnDefinition> whereToUpdate,
            List<PostgresColumnDefinition> columnsToUpdate ) {
        checkArgument( !columnsToUpdate.isEmpty(), "Columns to update must be specified." );
        checkArgument( !whereToUpdate.isEmpty(), "Columns for where clause must be specified." );

        if ( this.columns.containsAll( columnsToUpdate ) && this.columns.containsAll( whereToUpdate ) ) {
            //TODO: Warn when where clause is unindexed and will trigger a table scan.
            StringBuilder updateSql = new StringBuilder( "UPDATE " ).append( name );
            updateSql.append( " SET " )
                    .append( columnsToUpdate.stream()
                            .map( PostgresColumnDefinition::getName )
                            .map( columnName -> columnName + " = ? " )
                            .collect( Collectors.joining( "," ) ) );

            return updateSql.toString();
        } else {
            List<String> missingColumns = Stream.concat( columnsToUpdate.stream(), whereToUpdate.stream() )
                    .filter( c -> !this.columns.contains( c ) )
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.toList() );
            String errMsg = "Table is missing requested columns: " + String.valueOf( missingColumns );
            logger.error( errMsg );
            throw new IllegalArgumentException( errMsg );
        }
    }

    public String deleteQuery( List<PostgresColumnDefinition> whereToDelete ) {
        checkArgument( !whereToDelete.isEmpty(), "Columns for where clause must be specified." );

        if ( this.columns.containsAll( whereToDelete ) ) {
            //TODO: Warn when where clause is unindexed and will trigger a table scan.
            StringBuilder deleteSql = new StringBuilder( "DELETE FROM " ).append( name );
            deleteSql.append( " WHERE " )
                    .append( whereToDelete.stream()
                            .map( PostgresColumnDefinition::getName )
                            .map( columnName -> columnName + " = ? " )
                            .collect( Collectors.joining( " and " ) ) );

            return deleteSql.toString();
        } else {
            List<String> missingColumns = whereToDelete.stream()
                    .filter( c -> !this.columns.contains( c ) )
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.toList() );
            String errMsg = "Table is missing requested columns: " + String.valueOf( missingColumns );
            logger.error( errMsg );
            throw new IllegalArgumentException( errMsg );
        }

    }

    public String selectQuery( List<PostgresColumnDefinition> columnsToSelect ) {
        return selectQuery( columnsToSelect, ImmutableList.of() );
    }

    public String selectQuery(
            List<PostgresColumnDefinition> columnsToSelect,
            List<PostgresColumnDefinition> whereToSelect ) {
        checkArgument( !columnsToSelect.isEmpty(), "Columns for where clause must be specified." );

        if ( this.columns.containsAll( columnsToSelect ) ) {
            //TODO: Warn when where clause is unindexed and will trigger a table scan.
            StringBuilder selectSql = new StringBuilder( "SELECT " ).append( name );
            if ( columnsToSelect.isEmpty() ) {
                selectSql.append( " * " );
            } else {
                selectSql.append( columnsToSelect.stream()
                        .map( PostgresColumnDefinition::getName )
                        .collect( Collectors.joining( ", " ) ) );
            }
            if ( !whereToSelect.isEmpty() ) {
                selectSql.append( " WHERE " )
                        .append( columnsToSelect.stream()
                                .map( PostgresColumnDefinition::getName )
                                .map( columnName -> columnName + " = ? " )
                                .collect( Collectors.joining( " and " ) ) );
            }
            return selectSql.toString();
        } else {
            List<String> missingColumns = Stream.concat( columnsToSelect.stream(), whereToSelect.stream() )
                    .filter( c -> !this.columns.contains( c ) )
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.toList() );
            String errMsg = "Table is missing requested columns: " + String.valueOf( missingColumns );
            logger.error( errMsg );
            throw new IllegalArgumentException( errMsg );
        }

    }

    @Override
    public Stream<String> getCreateIndexQueries() {
        return indexes.stream().map( PostgresIndexDefinition::sql );
    }

    private void validate() {
        columns.stream()
                .collect( Collectors.groupingBy( PostgresColumnDefinition::getName ) )
                .forEach( ( lhs, rhs ) -> checkState( rhs.size() == 1,
                        "Detected duplicate column: %s", lhs ) );

        primaryKey.stream()
                .collect( Collectors.groupingBy( PostgresColumnDefinition::getName ) )
                .forEach( ( lhs, rhs ) -> checkState( rhs.size() == 1,
                        "Detected duplicate primary key column: %s",
                        lhs ) );

        //TODO: Add validation on indices.

    }

    @Override public String toString() {
        return "PostgresTableDefinition{" +
                "name='" + name + '\'' +
                ", primaryKey=" + primaryKey +
                ", columns=" + columns +
                ", unique=" + unique +
                ", indexes=" + indexes +
                ", ifNotExists=" + ifNotExists +
                '}';
    }

    @Override public boolean equals( Object o ) {
        if ( this == o ) { return true; }
        if ( !( o instanceof PostgresTableDefinition ) ) { return false; }

        PostgresTableDefinition that = (PostgresTableDefinition) o;

        if ( ifNotExists != that.ifNotExists ) { return false; }
        if ( name != null ? !name.equals( that.name ) : that.name != null ) { return false; }
        if ( primaryKey != null ? !primaryKey.equals( that.primaryKey ) : that.primaryKey != null ) { return false; }
        if ( columns != null ? !columns.equals( that.columns ) : that.columns != null ) { return false; }
        if ( unique != null ? !unique.equals( that.unique ) : that.unique != null ) { return false; }
        return indexes != null ? indexes.equals( that.indexes ) : that.indexes == null;
    }

    @Override public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + ( primaryKey != null ? primaryKey.hashCode() : 0 );
        result = 31 * result + ( columns != null ? columns.hashCode() : 0 );
        result = 31 * result + ( unique != null ? unique.hashCode() : 0 );
        result = 31 * result + ( indexes != null ? indexes.hashCode() : 0 );
        result = 31 * result + ( ifNotExists ? 1 : 0 );
        return result;
    }
}