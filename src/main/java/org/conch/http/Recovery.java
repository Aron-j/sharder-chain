/*
 *  Copyright © 2017-2018 Sharder Foundation.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  version 2 as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, you can visit it at:
 *  https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 *
 *  This software uses third party libraries and open-source programs,
 *  distributed under licenses described in 3RD-PARTY-LICENSES.
 *
 */

package org.conch.http;

import org.conch.Conch;
import org.conch.account.Account;
import org.conch.mint.pool.SharderPoolProcessor;
import org.conch.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author jangbubai
 */
public final class Recovery extends APIServlet.APIRequestHandler {

    static final Recovery INSTANCE = new Recovery();
    private static final List<String> RESET_PARAMS = Arrays.asList(
            "sharder.adminPassword",
            "sharder.disableAdminPassword",
            "sharder.useNATService",
            "sharder.myAddress",
            "sharder.NATClientKey",
            "sharder.NATServicePort",
            "sharder.NATServiceAddress",
            "sharder.HubBind",
            "sharder.HubBindAddress",
            "sharder.HubBindPassPhrase"
    );
    private static final HashMap<String, String> RESET_MAP;

    static {
        RESET_MAP = new HashMap<>(16);
        RESET_PARAMS.forEach(param -> RESET_MAP.put(param, ""));
    }

    private Recovery() {
        super(new APITag[]{APITag.DEBUG}, "restart");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        JSONObject response = new JSONObject();
        boolean restart = "true".equalsIgnoreCase(req.getParameter("restart"));
        long creatorId = Account.rsAccountToId(Conch.getStringProperty("sharder.HubBindAddress"));

        if (SharderPoolProcessor.whetherCreatorHasWorkingMinePool(creatorId)) {
            response.put("done", false);
            response.put("failedReason", "user has created a working pool, failed to recovery hub");
            return response;
        }
        try {
            // delete db
            Conch.getBlockchainProcessor().fullReset();
            // reset user define properties file
            Conch.storePropertiesToFile(RESET_MAP);
            // delete log files when resetting configuration
            String logPath = Conch.getUserHomeDir() + File.separator + "logs";
            File logFiles = new File(logPath);
            File[] files = logFiles.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    Logger.logInfoMessage("Deleting log files..." + file.getAbsolutePath());
                    PrintWriter writer = new PrintWriter(file);
                    writer.print("");
                    writer.close();
                }
            }

            response.put("done", true);
        } catch (RuntimeException | FileNotFoundException e) {
            JSONData.putException(response, e);
        }
        if (restart) {
            new Thread(() -> Conch.restartApplication(null)).start();
        }
        return response;
    }

    @Override
    protected final boolean requirePost() {
        return true;
    }

    @Override
    protected boolean requirePassword() {
        return true;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

}