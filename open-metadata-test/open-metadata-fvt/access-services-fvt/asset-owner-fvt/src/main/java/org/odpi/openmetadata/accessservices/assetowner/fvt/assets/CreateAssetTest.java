/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.accessservices.assetowner.fvt.assets;

import org.odpi.openmetadata.accessservices.assetowner.client.AssetOwner;
import org.odpi.openmetadata.accessservices.assetowner.client.rest.AssetOwnerRESTClient;
import org.odpi.openmetadata.accessservices.assetowner.metadataelements.AssetElement;
import org.odpi.openmetadata.accessservices.assetowner.properties.AssetProperties;
import org.odpi.openmetadata.accessservices.assetowner.properties.PrimitiveSchemaTypeProperties;
import org.odpi.openmetadata.adminservices.configuration.registration.AccessServiceDescription;
import org.odpi.openmetadata.frameworks.auditlog.AuditLog;
import org.odpi.openmetadata.fvt.utilities.FVTResults;
import org.odpi.openmetadata.fvt.utilities.auditlog.FVTAuditLogDestination;
import org.odpi.openmetadata.fvt.utilities.exceptions.FVTUnexpectedCondition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * CreateAssetTest calls the AssetOwnerClient to create an asset with with attachments
 * and then retrieve the results.
 */
public class CreateAssetTest
{
    private final static String testCaseName       = "CreateAssetTest";

    private final static int    maxPageSize        = 100;

    /*
     * The asset name is constant - the guid is created as part of the test.
     */
    private final static String assetName            = "TestAsset qualifiedName";
    private final static String assetDisplayName     = "Asset displayName";
    private final static String assetDescription     = "Asset description";
    private final static String assetAdditionalPropertyName  = "TestAsset additionalPropertyName";
    private final static String assetAdditionalPropertyValue = "TestAsset additionalPropertyValue";

    /*
     * The schemaType name is constant - the guid is created as part of the test.
     */
    private final static String schemaTypeName         = "SchemaType qualifiedNAme";
    private final static String schemaTypeDisplayName  = "SchemaType displayName";
    private final static String schemaTypeDescription  = "SchemaType description";
    private final static String schemaTypeType         = "SchemaType type";
    private final static String schemaTypeDefaultValue = "SchemaType defaultValue";


    /**
     * Run all of the defined tests and capture the results.
     *
     * @param serverName name of the server to connect to
     * @param serverPlatformRootURL the network address of the server running the OMAS REST servers
     * @param userId calling user
     * @return  results of running test
     */
    public static FVTResults performFVT(String   serverName,
                                        String   serverPlatformRootURL,
                                        String   userId)
    {
        FVTResults results = new FVTResults(testCaseName);

        results.incrementNumberOfTests();
        try
        {
            CreateAssetTest.runIt(serverPlatformRootURL, serverName, userId, results.getAuditLogDestination());
            results.incrementNumberOfSuccesses();
        }
        catch (Exception error)
        {
            results.addCapturedError(error);
        }

        return results;
    }


    /**
     * Run all of the tests in this class.
     *
     * @param serverPlatformRootURL root url of the server
     * @param serverName name of the server
     * @param userId calling user
     * @param auditLogDestination logging destination
     * @throws FVTUnexpectedCondition the test case failed
     */
    private static void runIt(String                 serverPlatformRootURL,
                              String                 serverName,
                              String                 userId,
                              FVTAuditLogDestination auditLogDestination) throws FVTUnexpectedCondition
    {
        CreateAssetTest thisTest = new CreateAssetTest();

        AuditLog auditLog = new AuditLog(auditLogDestination,
                                         AccessServiceDescription.ASSET_OWNER_OMAS.getAccessServiceCode(),
                                         AccessServiceDescription.ASSET_OWNER_OMAS.getAccessServiceName(),
                                         AccessServiceDescription.ASSET_OWNER_OMAS.getAccessServiceDescription(),
                                         AccessServiceDescription.ASSET_OWNER_OMAS.getAccessServiceWiki());

        AssetOwner client = thisTest.getAssetOwnerClient(serverName, serverPlatformRootURL, auditLog);
        String assetGUID = thisTest.getAsset(client, userId);
        String schemaTypeGUID = thisTest.getSchemaType(client, assetGUID, userId);
    }


    /**
     * Create and return an AssetOwner client.
     *
     * @param serverName name of the server to connect to
     * @param serverPlatformRootURL the network address of the server running the OMAS REST servers
     * @param auditLog logging destination
     * @return client
     * @throws FVTUnexpectedCondition the test case failed
     */
    private AssetOwner getAssetOwnerClient(String   serverName,
                                           String   serverPlatformRootURL,
                                           AuditLog auditLog) throws FVTUnexpectedCondition
    {
        final String activityName = "getAssetOwnerClient";

        try
        {
            AssetOwnerRESTClient restClient = new AssetOwnerRESTClient(serverName, serverPlatformRootURL);

            return new AssetOwner(serverName, serverPlatformRootURL, restClient, maxPageSize, auditLog);
        }
        catch (Exception unexpectedError)
        {
            throw new FVTUnexpectedCondition(testCaseName, activityName, unexpectedError);
        }
    }



    /**
     * Create an asset and return its GUID.
     *
     * @param client interface to Asset Owner OMAS
     * @param userId calling user
     * @return GUID of asset
     * @throws FVTUnexpectedCondition the test case failed
     */
    private String getAsset(AssetOwner client,
                            String     userId) throws FVTUnexpectedCondition
    {
        final String activityName = "getAsset";

        try
        {
            Map<String, String> additionalProperties = new HashMap<>();
            additionalProperties.put(assetAdditionalPropertyName, assetAdditionalPropertyValue);

            AssetProperties properties = new AssetProperties();

            properties.setTypeName("Asset");
            properties.setQualifiedName(assetName);
            properties.setDisplayName(assetDisplayName);
            properties.setDescription(assetDescription);
            properties.setAdditionalProperties(additionalProperties);

            String assetGUID = client.addAssetToCatalog(userId, properties);

            if (assetGUID == null)
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(no GUID for Create)");
            }

            AssetElement    retrievedElement    = client.getAssetSummary(userId, assetGUID);
            AssetProperties retrievedAsset = retrievedElement.getAssetProperties();

            if (retrievedAsset == null)
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(no SchemaType from Retrieve)");
            }

            if (! assetName.equals(retrievedAsset.getQualifiedName()))
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(Bad qualifiedName from Retrieve)");
            }
            if (! assetDisplayName.equals(retrievedAsset.getDisplayName()))
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(Bad displayName from Retrieve)");
            }
            if (! assetDescription.equals(retrievedAsset.getDescription()))
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(Bad description from Retrieve)");
            }
            if (retrievedAsset.getAdditionalProperties() == null)
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(null additionalProperties from Retrieve)");
            }
            else if (! assetAdditionalPropertyValue.equals(retrievedAsset.getAdditionalProperties().get(assetAdditionalPropertyName)))
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(bad additionalProperties from Retrieve)");
            }

            List<AssetElement> assetList = client.getAssetsByName(userId, assetName, 0, maxPageSize);

            if (assetList == null)
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(no Asset for RetrieveByName)");
            }
            else if (assetList.isEmpty())
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(Empty Asset list for RetrieveByName)");
            }
            else if (assetList.size() != 1)
            {
                throw new FVTUnexpectedCondition(testCaseName,
                                                 activityName + "(Asset list for RetrieveByName contains" + assetList.size() +
                                                         " elements)");
            }

            retrievedElement = assetList.get(0);
            retrievedAsset = retrievedElement.getAssetProperties();

            if (! assetName.equals(retrievedAsset.getQualifiedName()))
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(Bad qualifiedName from RetrieveByName)");
            }
            if (! assetDisplayName.equals(retrievedAsset.getDisplayName()))
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(Bad displayName from RetrieveByName)");
            }
            if (! assetDescription.equals(retrievedAsset.getDescription()))
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(Bad description from RetrieveByName)");
            }
            if (retrievedAsset.getAdditionalProperties() == null)
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(null additionalProperties from Retrieve)");
            }
            else if (! assetAdditionalPropertyValue.equals(retrievedAsset.getAdditionalProperties().get(assetAdditionalPropertyName)))
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(bad additionalProperties from Retrieve)");
            }

            return assetGUID;
        }
        catch (FVTUnexpectedCondition testCaseError)
        {
            throw testCaseError;
        }
        catch (Exception unexpectedError)
        {
            throw new FVTUnexpectedCondition(testCaseName, activityName, unexpectedError);
        }
    }


    /**
     * Create a schemaType and return its GUID.
     *
     * @param client interface to Asset Owner OMAS
     * @param assetGUID unique id of the schemaType manager
     * @param userId calling user
     * @return GUID of schemaType
     * @throws FVTUnexpectedCondition the test case failed
     */
    private String getSchemaType(AssetOwner client,
                                 String     assetGUID,
                                 String     userId) throws FVTUnexpectedCondition
    {
        final String activityName = "getSchemaType";

        try
        {
            PrimitiveSchemaTypeProperties properties = new PrimitiveSchemaTypeProperties();

            properties.setQualifiedName(schemaTypeName);
            properties.setDisplayName(schemaTypeDisplayName);
            properties.setDescription(schemaTypeDescription);
            properties.setDataType(schemaTypeType);
            properties.setDefaultValue(schemaTypeDefaultValue);
            properties.setTypeName("PrimitiveSchemaType");

            String schemaTypeGUID = client.addSchemaTypeToAsset(userId, assetGUID, properties);

            if (schemaTypeGUID == null)
            {
                throw new FVTUnexpectedCondition(testCaseName, activityName + "(no GUID for Create)");
            }

            return schemaTypeGUID;
        }
        catch (FVTUnexpectedCondition testCaseError)
        {
            throw testCaseError;
        }
        catch (Exception unexpectedError)
        {
            throw new FVTUnexpectedCondition(testCaseName, activityName, unexpectedError);
        }
    }
}
