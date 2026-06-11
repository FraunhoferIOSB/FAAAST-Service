/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util;

import com.auth0.jwt.interfaces.Claim;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.MatchExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Value;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;


/**
 * Helps to inject request claims into a nested logical expression.
 */
public class ExpressionInjectionHelper {

    private ExpressionInjectionHelper() {

    }


    /**
     * Inject a set of claims and global attributes into a LogicalExpression ("formula").
     *
     * @param formula The formula to inject into.
     * @param claims The claims to inject.
     */
    public static void injectLogicalExpression(LogicalExpression formula, Map<String, Claim> claims) {
        if (!formula.get$and().isEmpty()) {
            // It is an AND expression
            formula.get$and().forEach(op -> injectLogicalExpression(op, claims));
        }
        else if (!formula.get$or().isEmpty()) {
            // It is an OR expression
            formula.get$or().forEach(op -> injectLogicalExpression(op, claims));
        }
        else if (formula.get$not() != null) {
            // It is an OR expression
            injectLogicalExpression(formula.get$not(), claims);
        }
        else if (!formula.get$match().isEmpty()) {
            // It is an OR expression
            formula.get$match().forEach(op -> injectMatchExpression(op, claims));
        }
        else if (!formula.get$eq().isEmpty()) {
            formula.get$eq().forEach(val -> injectValue(val, claims));
        }
        else if (!formula.get$ne().isEmpty()) {
            formula.get$ne().forEach(val -> injectValue(val, claims));
        }
        else if (!formula.get$gt().isEmpty()) {
            formula.get$gt().forEach(val -> injectValue(val, claims));
        }
        else if (!formula.get$ge().isEmpty()) {
            formula.get$ge().forEach(val -> injectValue(val, claims));
        }
        else if (!formula.get$lt().isEmpty()) {
            formula.get$lt().forEach(val -> injectValue(val, claims));
        }
        else if (!formula.get$le().isEmpty()) {
            formula.get$le().forEach(val -> injectValue(val, claims));
        }
        else if (!formula.get$contains().isEmpty()) {
            formula.get$contains().forEach(val -> injectStringValue(val, claims));
        }
        else if (!formula.get$startsWith().isEmpty()) {
            formula.get$contains().forEach(val -> injectStringValue(val, claims));
        }
        else if (!formula.get$endsWith().isEmpty()) {
            formula.get$contains().forEach(val -> injectStringValue(val, claims));
        }
        else if (!formula.get$regex().isEmpty()) {
            formula.get$contains().forEach(val -> injectStringValue(val, claims));
        }
    }


    private static void injectMatchExpression(MatchExpression formula, Map<String, Claim> claims) {
        if (!formula.get$match().isEmpty()) {
            // It is an AND expression
            formula.get$match().forEach(op -> injectMatchExpression(op, claims));
        }
        else if (!formula.get$eq().isEmpty()) {
            formula.get$eq().forEach(val -> injectValue(val, claims));
        }
        else if (!formula.get$ne().isEmpty()) {
            formula.get$ne().forEach(val -> injectValue(val, claims));
        }
        else if (!formula.get$gt().isEmpty()) {
            formula.get$gt().forEach(val -> injectValue(val, claims));
        }
        else if (!formula.get$ge().isEmpty()) {
            formula.get$ge().forEach(val -> injectValue(val, claims));
        }
        else if (!formula.get$lt().isEmpty()) {
            formula.get$lt().forEach(val -> injectValue(val, claims));
        }
        else if (!formula.get$le().isEmpty()) {
            formula.get$le().forEach(val -> injectValue(val, claims));
        }
        else if (!formula.get$contains().isEmpty()) {
            formula.get$contains().forEach(val -> injectStringValue(val, claims));
        }
        else if (!formula.get$startsWith().isEmpty()) {
            formula.get$contains().forEach(val -> injectStringValue(val, claims));
        }
        else if (!formula.get$endsWith().isEmpty()) {
            formula.get$contains().forEach(val -> injectStringValue(val, claims));
        }
        else if (!formula.get$regex().isEmpty()) {
            formula.get$contains().forEach(val -> injectStringValue(val, claims));
        }
    }


    private static void injectStringValue(StringValue stringValue, Map<String, Claim> claims) {
        if (stringValue.get$attribute() != null) {
            if (stringValue.get$attribute().getClaim() != null) {
                stringValue.set$strVal(claims.get(stringValue.get$attribute().getClaim()).asString());
            }
        }
        else if (stringValue.get$strCast() != null) {
            injectValue(stringValue.get$strCast(), claims);
            return;
        }
        stringValue.set$attribute(null);
    }


    private static void injectValue(Value value, Map<String, Claim> claims) {
        if (value.get$attribute() == null) {
            return;
        }
        if (value.get$attribute().getClaim() != null) {
            value.set$strVal(claims.get(value.get$attribute().getClaim()).asString());
        }
        else if (value.get$attribute().getGlobal() != null) {
            AttributeItem.Global global = value.get$attribute().getGlobal();
            if (global == AttributeItem.Global.UTCNOW) {
                value.set$timeVal(LocalTime.now(Clock.systemUTC()).toString());
            }
            else if (global == AttributeItem.Global.LOCALNOW) {
                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                df.setTimeZone(tz);
                String nowAsISO = df.format(new Date());
                value.set$timeVal(nowAsISO);
            }
            else if (global == AttributeItem.Global.CLIENTNOW) {
                if (claims.containsKey("iat")) {
                    TimeZone tz = TimeZone.getDefault();
                    DateFormat df = new SimpleDateFormat("HH:mm:ss");
                    df.setTimeZone(tz);
                    String nowAsISO = df.format(claims.get("iat").asDate());
                    value.set$timeVal(nowAsISO);
                }
                else {
                    value.set$boolean(false);
                }

            }
            else if (global == AttributeItem.Global.ANONYMOUS) {
                value.set$boolean(true);
            }
            else {
                throw new IllegalArgumentException(String.format("Unknown attribute: %s", global));
            }
        }
        else if (value.get$strCast() != null) {
            injectValue(value.get$strCast(), claims);
            return;
        }
        else if (value.get$numCast() != null) {
            injectValue(value.get$numCast(), claims);
            return;
        }
        else if (value.get$hexCast() != null) {
            injectValue(value.get$hexCast(), claims);
            return;
        }
        else if (value.get$boolCast() != null) {
            injectValue(value.get$boolCast(), claims);
            return;
        }
        else if (value.get$timeCast() != null) {
            injectValue(value.get$timeCast(), claims);
            return;
        }
        else if (value.get$dateTimeCast() != null) {
            injectValue(value.get$dateTimeCast(), claims);
            return;
        }
        value.set$attribute(null);
    }

}
