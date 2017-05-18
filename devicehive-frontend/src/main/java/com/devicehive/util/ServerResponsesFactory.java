package com.devicehive.util;

/*
 * #%L
 * DeviceHive Java Server Common business logic
 * %%
 * Copyright (C) 2016 DataArt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.devicehive.configuration.Constants;
import com.devicehive.configuration.Messages;
import com.devicehive.exceptions.HiveException;
import com.devicehive.json.GsonFactory;
import com.devicehive.json.strategies.JsonPolicyDef;
import com.devicehive.model.DeviceCommand;
import com.devicehive.model.DeviceNotification;
import com.devicehive.model.JsonStringWrapper;
import com.devicehive.vo.DeviceVO;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletResponse;

import java.util.Random;

import static com.devicehive.json.strategies.JsonPolicyDef.Policy.*;

public class ServerResponsesFactory {

    public static JsonObject createNotificationInsertMessage(DeviceNotification deviceNotification, String subId) {
        JsonElement deviceNotificationJson =
                GsonFactory.createGson(NOTIFICATION_TO_CLIENT).toJsonTree(deviceNotification);
        JsonObject resultMessage = new JsonObject();
        resultMessage.addProperty("action", "notification/insert");
        resultMessage.addProperty(Constants.DEVICE_GUID, deviceNotification.getDeviceGuid());
        resultMessage.add(Constants.NOTIFICATION, deviceNotificationJson);
        resultMessage.addProperty(Constants.SUBSCRIPTION_ID, subId);
        return resultMessage;
    }

    public static JsonObject createCommandInsertMessage(DeviceCommand deviceCommand, String subId) {

        JsonElement deviceCommandJson = GsonFactory.createGson(COMMAND_TO_DEVICE).toJsonTree(deviceCommand,
                DeviceCommand.class);

        JsonObject resultJsonObject = new JsonObject();
        resultJsonObject.addProperty("action", "command/insert");
        resultJsonObject.addProperty(Constants.DEVICE_GUID, deviceCommand.getDeviceGuid());
        resultJsonObject.add(Constants.COMMAND, deviceCommandJson);
        resultJsonObject.addProperty(Constants.SUBSCRIPTION_ID, subId);
        return resultJsonObject;
    }

    public static JsonObject createCommandUpdateMessage(DeviceCommand deviceCommand) {
        JsonElement deviceCommandJson =
                GsonFactory.createGson(COMMAND_UPDATE_TO_CLIENT).toJsonTree(deviceCommand);
        JsonObject resultJsonObject = new JsonObject();
        resultJsonObject.addProperty("action", "command/update");
        resultJsonObject.add(Constants.COMMAND, deviceCommandJson);
        return resultJsonObject;
    }

    public static String parseNotificationStatus(DeviceNotification notificationMessage) {
        Assert.notNull(notificationMessage.getParameters(), "Notification parameters are required.");
        String jsonParametersString = notificationMessage.getParameters().getJsonString();
        Gson gson = GsonFactory.createGson();
        JsonElement parametersJsonElement = gson.fromJson(jsonParametersString, JsonElement.class);
        JsonObject statusJsonObject;
        if (parametersJsonElement instanceof JsonObject) {
            statusJsonObject = (JsonObject) parametersJsonElement;
        } else {
            throw new HiveException(Messages.PARAMS_NOT_JSON, HttpServletResponse.SC_BAD_REQUEST);
        }

        JsonElement jsonElement = statusJsonObject.get(Constants.STATUS);
        Assert.notNull(jsonElement, "Parameter " + Constants.STATUS + " is required.");

        return jsonElement.getAsString();
    }

    public static DeviceNotification createNotificationForDevice(DeviceVO device, String notificationName) {
        DeviceNotification notification = new DeviceNotification();
        notification.setId(Math.abs(new Random().nextInt())); // TODO: remove this when id generation will be moved to backend
        notification.setNotification(notificationName);
        notification.setDeviceGuid(device.getGuid());
        Gson gson = GsonFactory.createGson(JsonPolicyDef.Policy.DEVICE_PUBLISHED);
        JsonElement deviceAsJson = gson.toJsonTree(device);
        JsonStringWrapper wrapperOverDevice = new JsonStringWrapper(deviceAsJson.toString());
        notification.setParameters(wrapperOverDevice);
        return notification;
    }
}
