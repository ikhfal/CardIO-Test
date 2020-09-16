package com.idemia.tec.jkt.cardiotest.service;

import com.idemia.tec.jkt.cardiotest.model.CardioUser;
import com.imperva.ddc.core.query.*;
import com.imperva.ddc.service.DirectoryConnectorService;

public class ActiveDirectoryService {

    public static boolean authenticate(CardioUser cardioUser) {
        Endpoint endpoint = new Endpoint();
        endpoint.setSecuredConnection(false);
        endpoint.setPort(389);
        endpoint.setHost(cardioUser.getDomain());
        endpoint.setUserAccountName(cardioUser.getUserName());
        endpoint.setPassword(cardioUser.getSecurityToken());

        ConnectionResponse connectionResponse = DirectoryConnectorService.authenticate(endpoint);
        if (connectionResponse.getConnectionResultByHost().get(cardioUser.getDomain()))
            return !connectionResponse.isError();
        else return false;
    }

}
