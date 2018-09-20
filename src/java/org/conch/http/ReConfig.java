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
import org.conch.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public final class ReConfig extends APIServlet.APIRequestHandler {

    static final ReConfig instance = new ReConfig();
    static final List<String> excludeParams = Arrays.asList("restart", "adminPassword");
    private ReConfig() {
        super(new APITag[] {APITag.DEBUG}, "restart");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        JSONObject response = new JSONObject();
        boolean restart = "true".equalsIgnoreCase(req.getParameter("restart"));
        HashMap map = new HashMap();
        Enumeration enu = req.getParameterNames();
        while(enu.hasMoreElements()) {
            String paraName = (String)enu.nextElement();
            if (excludeParams.contains(paraName)) {
                continue;
            }
            map.put(paraName, req.getParameter(paraName));
        }
        Conch.storePropertiesToFile(map);
        if (restart) {
            Conch.restartApplication(null);
        }
        response.put("reconfiged", true);
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
