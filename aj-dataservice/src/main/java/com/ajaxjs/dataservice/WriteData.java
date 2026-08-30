package com.ajaxjs.dataservice;

import com.ajaxjs.dataservice.model.Endpoint;
import com.ajaxjs.spring.traceid.TraceXFilter;
import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.crud.Create;
import com.ajaxjs.sqlman.crud.Update;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.MapTool;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.UrlEncode;
import com.ajaxjs.util.httpremote.HttpConstant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Write logic for Data service.
 */
@RequiredArgsConstructor
public class WriteData {
    /**
     * The request object.
     */
    final HttpServletRequest req;

    /**
     * The endpoint object, containing the endpoint info.
     */
    final Endpoint endpoint;

    /**
     * The patch params.
     */
    Map<String, String> patchParams;

    /**
     * 设置从 URL 模板中提取的路径参数。
     *
     * @param patchParams 路径参数名与参数值的映射
     * @return 当前写入处理器，便于链式调用
     */
    public WriteData setPatchParams(Map<String, String> patchParams) {
        this.patchParams = patchParams;

        return this;
    }

    /**
     * Do creation.
     *
     * @return The newly created id.
     */
    public Serializable create() {
        if (req.getContentType().contains(HttpConstant.CONTENT_TYPE_JSON))
            return createJson();
        else if (req.getContentType().contains(HttpConstant.CONTENT_TYPE_FORM))
            return createForm();
        else
            throw new UnsupportedOperationException("Unknown ContentType:" + req.getContentType());
    }

    /**
     * Do creation by inputting JSON data.
     *
     * @return The newly created id.
     */
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

    /**
     * Do creation by inputting FORM data.
     *
     * @return The newly created id.
     */
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

    /**
     * Do update
     *
     * @param idField The field name of the id.
     * @return The update result.
     */
    public UpdateResult update(String idField) {
        if (req.getContentType().contains(HttpConstant.CONTENT_TYPE_JSON))
            return updateJson(idField);
        else if (req.getContentType().contains(HttpConstant.CONTENT_TYPE_FORM))
            return updateForm(idField);
        else
            throw new UnsupportedOperationException("Unknown ContentType:" + req.getContentType());
    }

    /**
     * Do update by inputting JSON data.
     *
     * @param idField The field name of the id.
     * @return The update result.
     */
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

    /**
     * Do update by inputting FORM data.
     *
     * @param idField The field name of the id.
     * @return The update result.
     */
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

    /**
     * 读取并校验请求体中的原始数据。
     *
     * @return 非空的原始请求体
     * @throws IllegalArgumentException 当请求体为空或仅包含空白字符时抛出
     */
    private String getInputData() {
        String raw = TraceXFilter.getStreamBodyAsStr(req);

        if (ObjectHelper.isEmptyText(raw) || ObjectHelper.isEmptyText(raw.trim()))
            throw new IllegalArgumentException("The request body is empty.");

        return raw;
    }

    /**
     * 将当前路径参数与表单参数组合为更新操作的参数。
     *
     * @param action 要执行的 SQL 操作
     * @param mapParams 请求中的表单参数
     * @return 已绑定参数的更新操作
     */
    private Update combineParamsUpdate(Action action, Map<String, String> mapParams) {
        return combineParamsUpdate(action, patchParams, mapParams);
    }

    /**
     * 将路径参数与表单参数组合为更新操作的参数。
     *
     * @param action 要执行的 SQL 操作
     * @param patchParams URL 模板参数
     * @param mapParams 请求参数
     * @return 已绑定参数的更新操作
     */
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
     * 将路径参数与表单参数组合为创建操作的参数。
     *
     * @param action 要执行的 SQL 操作
     * @param mapParams 请求参数
     * @return 已绑定参数的创建操作
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
}
