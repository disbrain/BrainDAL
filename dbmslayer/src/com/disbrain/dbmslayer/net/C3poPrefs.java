package com.disbrain.dbmslayer.net;

import com.mchange.v2.c3p0.AbstractConnectionCustomizer;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: angel
 * Date: 20/12/13
 * Time: 17.10
 * To change this template use File | Settings | File Templates.
 */
public class C3poPrefs extends AbstractConnectionCustomizer {

    public void onAcquire(Connection c, String pdsIdt) throws SQLException {
        //System.err.println("Acquired " + c + " [" + pdsIdt + "]");

        // override the default transaction isolation of
        // newly acquired Connections
        c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        c.setAutoCommit(false);
        //System.err.println("Inside callback autocommit: "+c.getAutoCommit());
    }

    public void onCheckOut(Connection c, String parentDataSourceIdentityToken) throws SQLException {
        c.setAutoCommit(false);
        //System.err.println("Inside CHECKOUT callback autocommit: "+c.getAutoCommit());
    }

}
