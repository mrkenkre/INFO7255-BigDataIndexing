package com.restapi.repository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MedPlansDaoImpl implements MedPlansDao {
    @Autowired
    private RedisTemplate redisTemplate;
    private static final String KEY = "MEDPLAN";
    @Override
    public boolean saveMedplan(String medPlans) {

        try {
            redisTemplate.opsForValue().set(KEY, medPlans);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public String fetchAllMedPlans() {

        String medPlans = (String) redisTemplate.opsForValue().get(KEY);
        return medPlans;
    }
    @Override
    public String fetchMedPlanById(String id) {

        String medPlans;
        medPlans = (String) redisTemplate.opsForValue().get(KEY);
        if (medPlans != null) {
            try {
                JSONObject jsonObject = new JSONObject(medPlans);
                String objectId = jsonObject.getString("objectId");
                if (objectId.equals(id)) {
                    return jsonObject.toString();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    @Override
    public boolean deleteMedPlan(String id) {

        String medPlans = (String) redisTemplate.opsForValue().get(KEY);
        if (medPlans != null) {
            try {
                JSONObject jsonObject = new JSONObject(medPlans);
                String objectId = jsonObject.getString("objectId");
                if (objectId.equals(id)) {
                    redisTemplate.delete(KEY); // Delete the key from Redis
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
