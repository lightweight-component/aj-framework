package com.ajaxjs.framework.dataservice;

import com.ajaxjs.framework.dataservice.model.Endpoint;
import com.ajaxjs.spring.traceid.TraceXFilter;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman_v2.Action;
import com.ajaxjs.sqlman_v2.crud.Create;
import com.ajaxjs.sqlman_v2.crud.Update;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.MapTool;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.UrlEncode;
import com.ajaxjs.util.httpremote.HttpConstant;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Map;

@RequiredArgsConstructor
public class WriteData {
    final HttpServletRequest req;

    final Endpoint endpoint;

    Map<String, String> patchParams;

    public WriteData setPatchParams(Map<String, String> patchParams) {
        this.patchParams = patchParams;

        return this;
    }

    public Serializable create() {
        if (req.getContentType().contains(HttpConstant.CONTENT_TYPE_JSON))
            return createJson();
        else if (req.getContentType().contains(HttpConstant.CONTENT_TYPE_FORM))
            return createForm();
        else
            throw new UnsupportedOperationException("Unknown ContentType:" + req.getContentType());
    }

    public Serializable createJson() {
        String rawJson = getInputData();
        Map<String, Object> mapParams = JsonUtil.json2map(rawJson);

        if (endpoint.isAutoSql()) {
            if (ObjectHelper.isEmpty(mapParams))
                throw new IllegalArgumentException("The request body is empty.");

            return new Action(mapParams, endpoint.getTableName()).create().execute(endpoint.isAutoIns()).getNewlyId();
        } else {
            String sql = endpoint.getSql();
            Map<String, String> _mapParams = MapTool.as(mapParams, Object::toString);

            return combineParamsCreate(new Action(sql), _mapParams).execute(endpoint.isAutoIns()).getNewlyId();
        }
    }

    public Serializable createForm() {
        String raw = getInputData();
        Map<String, String> mapParams = UrlEncode.parseStringToMap(raw);

        if (endpoint.isAutoSql()) {
            Map<String, Object> dataObject = MapTool.as(mapParams, v -> v);

            return new Action(dataObject, endpoint.getTableName()).create().execute(endpoint.isAutoIns()).getNewlyId();
        } else {
            String sql = endpoint.getSql();

            return combineParamsCreate(new Action(sql), mapParams).execute(endpoint.isAutoIns()).getNewlyId();
        }
    }

    public UpdateResult update(String idField) {
        if (req.getContentType().contains(HttpConstant.CONTENT_TYPE_JSON))
            return updateJson(idField);
        else if (req.getContentType().contains(HttpConstant.CONTENT_TYPE_FORM))
            return updateForm(idField);
        else
            throw new UnsupportedOperationException("Unknown ContentType:" + req.getContentType());
    }

    public UpdateResult updateJson(String idField) {
        String rawJson = getInputData();
        Map<String, Object> mapParams = JsonUtil.json2map(rawJson);

        if (endpoint.isAutoSql()) {
            if (ObjectHelper.isEmpty(mapParams))// might be `{}`
                throw new IllegalArgumentException("The request body is empty.");

            Action action = new Action(mapParams, endpoint.getTableName());

            return action.update().withId(idField);
        } else {
            String sql = endpoint.getSql();
            Map<String, String> _mapParams = MapTool.as(mapParams, Object::toString);

            return combineParamsUpdate(new Action(sql), _mapParams).execute();
        }
    }

    public UpdateResult updateForm(String idField) {
        String raw = getInputData();
        Map<String, String> mapParams = UrlEncode.parseStringToMap(raw);

        if (endpoint.isAutoSql()) {
            Map<String, Object> dataObject = MapTool.as(mapParams, v -> v);

            return new Action(dataObject, endpoint.getTableName()).update().withId(idField);
        } else {
            String sql = endpoint.getSql();

            return combineParamsUpdate(new Action(sql), mapParams).execute();
        }
    }

    private String getInputData() {
        String raw = TraceXFilter.getStreamBodyAsStr(req);

        if (ObjectHelper.isEmptyText(raw) || ObjectHelper.isEmptyText(raw.trim()))
            throw new IllegalArgumentException("The request body is empty.");

        return raw;
    }

    /**
     * To deal with the query string and patch params, make them as one array.
     */
    Update combineParamsUpdate(Action action, Map<String, String> mapParams) {
        return combineParamsUpdate(action, patchParams, mapParams);
    }

    static Update combineParamsUpdate(Action action, Map<String, String> patchParams, Map<String, String> mapParams) {
        if (patchParams == null)
            if (mapParams.size() > 0)
                return action.update(mapParams);
            else
                return action.update();
        else {
            Object[] arr = DataServiceDispatcher.getQueryParams(mapParams, patchParams);

            return action.update(arr);
        }
    }

    /**
     * To deal with the query string and patch params, make them as one array.
     */
    Create combineParamsCreate(Action action, Map<String, String> mapParams) {
        if (patchParams == null)
            if (mapParams.size() > 0)
                return action.create(mapParams);
            else
                return action.create();
        else {
            Object[] arr = DataServiceDispatcher.getQueryParams(mapParams, patchParams);

            return action.create(arr);
        }
    }

    public boolean delete() {
        return false;
    }
}
