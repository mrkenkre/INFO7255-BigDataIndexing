package com.restapi.repository;
import com.restapi.validation.exceptions.MedPlanNotFoundException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class MedPlansDaoImpl implements MedPlansDao {
    @Autowired
    private RedisTemplate redisTemplate;

    Jedis jedis = new Jedis("localhost");
    //private static final String KEY = "12xvxc345ssdsds-508";
    @Override
    public String saveMedplan(String rawkey, JSONObject medPlans) {
        String key = "plan:" + rawkey;
        try {
            jsonToMap(medPlans);
            return "true";
        } catch (Exception e) {
            return "false";
            //throw new RuntimeException(e);
        }
      
    }
    public Map<String, Map<String, Object>> jsonToMap(JSONObject jsonObject) {
        Map<String, Map<String, Object>> map = new HashMap<>();
        Map<String, Object> contentMap = new HashMap<>();

        for (String key : jsonObject.keySet()) {
            String redisKey = jsonObject.get("objectType") + ":" + jsonObject.get("objectId");
            Object value = jsonObject.get(key);

            if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
                jedis.sadd(redisKey + ":" + key, ((Map<String, Map<String, Object>>) value).entrySet().iterator().next().getKey());
            } else if (value instanceof JSONArray) {
                value = jsonToList((JSONArray) value);
                ((List<Map<String, Map<String, Object>>>) value)
                        .forEach((entry) -> {
                            entry.keySet()
                                    .forEach((listKey) -> {
                                        jedis.sadd(redisKey + ":" + key, listKey);
                                    });
                        });
            } else {
                jedis.hset(redisKey, key, value.toString());
                contentMap.put(key, value);
                map.put(redisKey, contentMap);
            }
        }
        return map;
    }
    public List<Object> jsonToList(JSONArray jsonArray) {
        List<Object> result = new ArrayList<>();
        for (Object value : jsonArray) {
            if (value instanceof JSONArray) value = jsonToList((JSONArray) value);
            else if (value instanceof JSONObject) value = jsonToMap((JSONObject) value);
            result.add(value);
        }
        return result;
    }
    @Override
    public String fetchAllMedPlans() {
        List<String> medPlans = redisTemplate.opsForValue().multiGet(redisTemplate.keys("*"));
        if (medPlans != null) {
            return medPlans.stream()
                    .filter(value -> value != null)
                    .collect(Collectors.joining(", "));
        }
        return "";
    }

    public Map<String, Object> fetchMedPlanById(String id) {
        String key = "plan" + ":" + id;
        Map<String, Object> result = new HashMap<>();
        getOrDelete(key, result, false);
        return result;
    }
    private Map<String, Object> getOrDelete(String redisKey, Map<String, Object> resultMap, boolean isDelete) {
        Set<String> keys = jedis.keys(redisKey + ":*");
        keys.add(redisKey);

        for (String key : keys) {
            if (key.equals(redisKey)) {
                if (isDelete) jedis.del(new String[]{key});
                else {
                    Map<String, String> object = jedis.hgetAll(key);
                    for (String attrKey : object.keySet()) {
                        if (!attrKey.equalsIgnoreCase("eTag")) {
                            resultMap.put(attrKey, isInteger(object.get(attrKey)) ? Integer.parseInt(object.get(attrKey)) : object.get(attrKey));
                        }
                    }
                }
            } else {
                String newKey = key.substring((redisKey + ":").length());
                Set<String> members = jedis.smembers(key);
                if (members.size() > 1 || newKey.equals("linkedPlanServices")) {
                    List<Object> listObj = new ArrayList<>();
                    for (String member : members) {
                        if (isDelete) {
                            getOrDelete(member, null, true);
                        } else {
                            Map<String, Object> listMap = new HashMap<>();
                            listObj.add(getOrDelete(member, listMap, false));
                        }
                    }
                    if (isDelete) jedis.del(new String[]{key});
                    else resultMap.put(newKey, listObj);
                } else {
                    if (isDelete) {
                        jedis.del(new String[]{members.iterator().next(), key});
                    } else {
                        Map<String, String> object = jedis.hgetAll(members.iterator().next());
                        Map<String, Object> nestedMap = new HashMap<>();
                        for (String attrKey : object.keySet()) {
                            nestedMap.put(attrKey,
                                    isInteger(object.get(attrKey)) ? Integer.parseInt(object.get(attrKey)) : object.get(attrKey));
                        }
                        resultMap.put(newKey, nestedMap);
                    }
                }
            }
        }
        return resultMap;
    }
    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    @Override
    public boolean deleteMedPlan(String id)  throws MedPlanNotFoundException {
        String key = "plan" + ":" + id;
        Map<String, Object> OldPlan=fetchMedPlanById(id);
        if (OldPlan == null) {
            throw new MedPlanNotFoundException("Old plan not found for ID: " + id);
        }
        getOrDelete(key, null, true);
        return true;
    }

    @Override
    public boolean saveETag(String id, String etag) {

        System.out.println("ID: "+id);
        System.out.println("ETag: "+etag);
        try {
            jedis.set("id_" + id, etag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String fetchETag(String id) {

        String etag;
        etag = jedis.get("id_" + id);
        System.out.println("ID: "+id);
        System.out.println("ETag: "+etag);
        if (etag != null) {
            try {
                return etag;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return etag;
    }
}
