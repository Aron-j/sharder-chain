/*
 * Copyright © 2017 sharder.org.
 * Copyright © 2014-2017 ichaoj.com.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with ichaoj.com,
 * no part of the COS software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package org.conch.db;

import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Create Statement and PrepareStatement for use with FilteredConnection
 */
public interface FilteredFactory {

    /**
     * Create a FilteredStatement for the supplied Statement
     *
     * @param   stmt                Statement
     * @return                      Wrapped statement
     */
    Statement createStatement(Statement stmt);

    /**
     * Create a FilteredPreparedStatement for the supplied PreparedStatement
     *
     * @param   stmt                Prepared statement
     * @param   sql                 SQL statement
     * @return                      Wrapped prepared statement
     */
    PreparedStatement createPreparedStatement(PreparedStatement stmt, String sql);
}
