/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.controlServer;

import java.text.MessageFormat;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.db.Client;
import at.alladin.rmbt.db.Test;
import at.alladin.rmbt.db.fields.IntField;

public class ResultUpdateResource extends ServerResource
{
    @Post("json")
    public String request(final String entity)
    {
        addAllowOrigin();
        
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        
        final String ip = getIP();
        
        System.out.println(MessageFormat.format(labels.getString("NEW_RESULT_UPDATE"), ip));
        
        if (entity != null && !entity.isEmpty())
        {
            try
            {
                request = new JSONObject(entity);
                final UUID clientUUID = UUID.fromString(request.getString("uuid"));
                final UUID testUUID = UUID.fromString(request.getString("test_uuid"));
                final int zipCode = request.getInt("zip_code");
                
                final Client client = new Client(conn);
                final long clientId = client.getClientByUuid(clientUUID);
                if (clientId < 0)
                    throw new IllegalArgumentException("error while loading client");
                
                final Test test = new Test(conn);
                if (test.getTestByUuid(testUUID) < 0)
                    throw new IllegalArgumentException("error while loading test");
                
                if (test.getField("client_id").longValue() != clientId)
                    throw new IllegalArgumentException("client UUID does not match test");
                
                ((IntField)test.getField("zip_code")).setValue(zipCode);
                
                test.updateTest();
                
                if (test.hasError())
                    errorList.addError(test.getError());
            }
            catch (final JSONException e)
            {
                errorList.addError("ERROR_REQUEST_JSON");
                System.out.println("Error parsing JSDON Data " + e.toString());
            }
            catch (final IllegalArgumentException e)
            {
                errorList.addError("ERROR_REQUEST_JSON");
                System.out.println("Error parsing JSDON Data " + e.toString());
            }
        }
        
        return answer.toString();
    }
    
    @Get("json")
    public String retrieve(final String entity)
    {
        return request(entity);
    }
    
}
