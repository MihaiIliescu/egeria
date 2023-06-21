/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.assetcatalog.eventclient;

import org.odpi.openmetadata.accessservices.assetcatalog.api.AssetCatalogEventListener;
import org.odpi.openmetadata.accessservices.assetcatalog.connectors.outtopic.AssetCatalogOutTopicClientConnector;
import org.odpi.openmetadata.commonservices.ffdc.InvalidParameterHandler;
import org.odpi.openmetadata.frameworkservices.ocf.metadatamanagement.client.OCFRESTClient;
import org.odpi.openmetadata.frameworkservices.ocf.metadatamanagement.ffdc.OMAGOCFErrorCode;
import org.odpi.openmetadata.frameworkservices.ocf.metadatamanagement.rest.ConnectionResponse;
import org.odpi.openmetadata.frameworks.auditlog.AuditLog;
import org.odpi.openmetadata.frameworks.connectors.Connector;
import org.odpi.openmetadata.frameworks.connectors.ConnectorBroker;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectionCheckedException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.Connection;


/**
 * AssetCatalogEventClient provides the implementation to manage the interaction with the server to
 * set up a listener to support the receipt of inbound events from the Asset Catalog OMAS Out Topic.
 */
public class AssetCatalogEventClient {
    private static final String SERVICE_NAME = "Asset Catalog OMAS";

    private final String serverName;
    private final String serverPlatformURLRoot;
    private final String callerId;
    private final OCFRESTClient restClient;

    private final InvalidParameterHandler invalidParameterHandler = new InvalidParameterHandler();

    private AssetCatalogOutTopicClientConnector configurationEventTopicConnector = null;
    private AuditLog auditLog = null;

    /**
     * Create a new client with no authentication embedded in the HTTP request.
     *
     * @param serverName            name of the server to connect to
     * @param serverPlatformURLRoot the network address of the server running the OMAS REST services
     * @param callerId              unique identifier of the caller
     * @throws InvalidParameterException there is a problem creating the client-side components to issue any
     *                                   REST API calls.
     */
    public AssetCatalogEventClient(String serverName,
                                   String serverPlatformURLRoot,
                                   String callerId) throws InvalidParameterException {
        final String methodName = "Constructor (no security)";

        invalidParameterHandler.validateOMAGServerPlatformURL(serverPlatformURLRoot, serverName, methodName);

        this.serverName = serverName;
        this.serverPlatformURLRoot = serverPlatformURLRoot;
        this.restClient = new OCFRESTClient(serverName, serverPlatformURLRoot);
        this.callerId = callerId;
    }


    /**
     * Create a new client that passes userId and password in each HTTP request.  This is the
     * userId/password of the calling server.  The end user's userId is sent on each request.
     *
     * @param serverName            name of the server to connect to
     * @param serverPlatformURLRoot the network address of the server running the OMAS REST services
     * @param userId                caller's userId embedded in all HTTP requests
     * @param password              caller's userId embedded in all HTTP requests
     * @param callerId              unique identifier of the caller
     * @throws InvalidParameterException there is a problem creating the client-side components to issue any
     *                                   REST API calls.
     */
    public AssetCatalogEventClient(String serverName,
                                   String serverPlatformURLRoot,
                                   String userId,
                                   String password,
                                   String callerId) throws InvalidParameterException {
        final String methodName = "Constructor (with security)";

        invalidParameterHandler.validateOMAGServerPlatformURL(serverPlatformURLRoot, serverName, methodName);

        this.serverName = serverName;
        this.serverPlatformURLRoot = serverPlatformURLRoot;
        this.restClient = new OCFRESTClient(serverName, serverPlatformURLRoot, userId, password);
        this.callerId = callerId;
    }


    /**
     * Create a new client that is to be used within an OMAG Server.
     *
     * @param serverName            name of the server to connect to
     * @param serverPlatformURLRoot the network address of the server running the OMAS REST services
     * @param restClient            pre-initialized REST client
     * @param maxPageSize           pre-initialized parameter limit
     * @param auditLog              logging destination
     * @param callerId              unique identifier of the caller
     * @throws InvalidParameterException there is a problem with the information about the remote OMAS
     */
    public AssetCatalogEventClient(String serverName,
                                   String serverPlatformURLRoot,
                                   OCFRESTClient restClient,
                                   int maxPageSize,
                                   AuditLog auditLog,
                                   String callerId) throws InvalidParameterException {
        final String methodName = "Constructor (with REST Client)";

        invalidParameterHandler.setMaxPagingSize(maxPageSize);
        invalidParameterHandler.validateOMAGServerPlatformURL(serverPlatformURLRoot, serverName, methodName);

        this.serverName = serverName;
        this.serverPlatformURLRoot = serverPlatformURLRoot;
        this.restClient = restClient;
        this.auditLog = auditLog;
        this.callerId = callerId;
    }


    /**
     * Return the name of the server where configuration is supposed to be stored.
     *
     * @return server name
     */
    public String getConfigurationServerName() {
        return serverName;
    }


    /**
     * Register a listener object that will be passed each of the events published by
     * the Asset Catalog OMAS.
     *
     * @param userId   calling user
     * @param listener listener object
     * @throws InvalidParameterException  one of the parameters is null or invalid.
     * @throws ConnectionCheckedException there are errors in the configuration of the connection which is preventing
     *                                    the creation of a connector.
     * @throws ConnectorCheckedException  there are errors in the initialization of the connector.
     * @throws PropertyServerException    there is a problem retrieving information from the property server(s).
     * @throws UserNotAuthorizedException the requesting user is not authorized to issue this request.
     */
    public void registerListener(String userId,
                                 AssetCatalogEventListener listener) throws InvalidParameterException,
            ConnectionCheckedException,
            ConnectorCheckedException,
            PropertyServerException,
            UserNotAuthorizedException {
        final String methodName = "registerListener";
        final String nameParameter = "listener";

        final String urlTemplate = serverPlatformURLRoot + "/servers/{0}/open-metadata/access-services/asset-catalog/users/{1}/topics/out-topic-connection/{2}";

        invalidParameterHandler.validateUserId(userId, methodName);
        invalidParameterHandler.validateObject(listener, nameParameter, methodName);

        ConnectionResponse restResult = restClient.callOCFConnectionGetRESTCall(methodName,
                                                                                urlTemplate,
                                                                                serverName,
                                                                                userId,
                                                                                callerId);

        Connection topicConnection = restResult.getConnection();
        ConnectorBroker connectorBroker = new ConnectorBroker(auditLog);
        Connector connector = connectorBroker.getConnector(topicConnection);

        if (connector == null) {
            throw new ConnectorCheckedException(OMAGOCFErrorCode.NULL_CONNECTOR_RETURNED.getMessageDefinition(topicConnection.getQualifiedName(),
                    SERVICE_NAME,
                    serverName,
                    serverPlatformURLRoot),
                    this.getClass().getName(),
                    methodName);
        }
        if (connector instanceof AssetCatalogOutTopicClientConnector) {
            configurationEventTopicConnector = (AssetCatalogOutTopicClientConnector) connector;
            configurationEventTopicConnector.setAuditLog(auditLog);
            configurationEventTopicConnector.start();
        } else {
            throw new ConnectorCheckedException(OMAGOCFErrorCode.WRONG_TYPE_OF_CONNECTOR.getMessageDefinition(topicConnection.getQualifiedName(),
                    SERVICE_NAME,
                    serverName,
                    serverPlatformURLRoot,
                    AssetCatalogOutTopicClientConnector.class.getName()),
                    this.getClass().getName(),
                    methodName);
        }
        configurationEventTopicConnector.registerListener(userId, listener);
    }
}
