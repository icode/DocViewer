package com.log4ic.utils.convert.office.document;

import javolution.util.FastList;
import org.apache.commons.io.IOUtils;
import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.SimpleDocumentFormatRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 张立鑫 IntelligentCode
 * @date: 12-1-22
 * @time: 上午2:33
 */
public class OfficeDocumentFormatRegistry extends SimpleDocumentFormatRegistry {
    public OfficeDocumentFormatRegistry(InputStream input) throws IOException, JSONException {
        readJsonArray(IOUtils.toString(input));
    }

    public OfficeDocumentFormatRegistry(String source) throws JSONException {
        readJsonArray(source);
    }

    private void readJsonArray(String source) throws JSONException {
        JSONArray array = new JSONArray(source);
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonFormat = array.getJSONObject(i);
            DocumentFormat format = new DocumentFormat();
            format.setName(jsonFormat.getString("name"));
            format.setExtension(jsonFormat.getString("extension"));
            format.setMediaType(jsonFormat.getString("mediaType"));
            if (jsonFormat.has("inputFamily")) {
                format.setInputFamily(DocumentFamily.valueOf(jsonFormat.getString("inputFamily")));
            }
            if (jsonFormat.has("loadProperties")) {
                format.setLoadProperties(toJavaMap(jsonFormat.getJSONObject("loadProperties")));
            }
            if (jsonFormat.has("storePropertiesByFamily")) {
                JSONObject jsonStorePropertiesByFamily = jsonFormat.getJSONObject("storePropertiesByFamily");
                for (String key : JSONObject.getNames(jsonStorePropertiesByFamily)) {
                    Map<String, ?> storeProperties = toJavaMap(jsonStorePropertiesByFamily.getJSONObject(key));
                    format.setStoreProperties(DocumentFamily.valueOf(key), storeProperties);
                }
            }
            addFormat(format);
        }
    }

    private Map<String, ?> toJavaMap(JSONObject jsonMap) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        for (String key : JSONObject.getNames(jsonMap)) {
            Object value = jsonMap.get(key);
            if (value instanceof JSONObject) {
                map.put(key, toJavaMap((JSONObject) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    protected List<DocumentFormat> documentFormats = new FastList<DocumentFormat>();

    public void addFormat(DocumentFormat documentFormat) {
        super.addFormat(documentFormat);
        documentFormats.add(documentFormat);
    }

    public List<DocumentFormat> getDocumentFormats() {
        return new FastList<DocumentFormat>(documentFormats);
    }
}
