package wethinkcode.web;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import wethinkcode.loadshed.common.transfer.ScheduleDO;
import wethinkcode.loadshed.common.transfer.StageDO;
import wethinkcode.places.model.Town;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.jackson.JacksonObjectMapper;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static wethinkcode.stage.StageService.stages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Routes {
    public static final String LANDING_PAGE = "/";
    public static final String STAGE_PAGE = "/stage";
    public static final String PROVINCES_PAGE = "/provinces";
    public static final String TOWNS_PAGE = "/towns";
    public static final String SCHEDULE_PAGE = "/town_schedule";

    public static final String FETCH_TOWNS = "/provinces.action";

    public static final String FETCH_TOWN_STAGE = "townsStage.action";

    private static String places_url = "http://localhost:7000/";
    private static String stage_url = "http://localhost:7001/";
    private static String schedule_url = "http://localhost:7002/";

    public static void configure(Javalin app) {
        initJsonMapper();
        app.routes(() -> {
            get(LANDING_PAGE, landing);
            get(STAGE_PAGE, stage);
            get(PROVINCES_PAGE, provinces);
            get(TOWNS_PAGE, towns);
            get(SCHEDULE_PAGE, schedule);

            post(FETCH_TOWNS, view_towns);
            post(FETCH_TOWN_STAGE, view_stage);
        });
    }

    // HANDLERS
    public static Handler landing = ctx -> {
        Map<String, Object> model = Map.of("message", "Hello World!!! - From Thymeleaf");
        ctx.render("index.html", model);
    };



    private static final Logger logger = LoggerFactory.getLogger(Routes.class);

    public static Handler stage = ctx -> {
        ArrayList<StageDO> stages = new ArrayList<>();

        for (int i = 0; i < 9; i++) {stages.add(new StageDO(i));}

        HttpResponse<JsonNode> response = Unirest.get(stage_url+"stage").asJson();
        StageDO currentStage = new StageDO(getStageFromResponse(response));

        Map<String, Object> model = Map.of("stages", stages, "current_stage", currentStage);
        ctx.render("stage.html", model);
    };


    public static Handler provinces = ctx -> {
        HttpResponse<JsonNode> response = Unirest.get(places_url+"provinces").asJson();
        ArrayList<String> provincesList = getListFromResponse(response);

        Map<String, Object> model = Map.of("provinces", provincesList);
        ctx.render("provinces.html", model);
    };

    public static Handler towns = ctx -> {
        ctx.render("towns.html");
    };

    public static Handler view_towns = ctx -> {
        String selectedProvince = ctx.formParamAsClass("province", String.class)
                .check(Objects::nonNull, "Province was not specified")
                .get();

        // Fetch towns based on the selected province
        HttpResponse<JsonNode> response = Unirest.get(places_url + "towns/" + selectedProvince).asJson();

        // If the response is empty or invalid, handle the error
        if (response.getStatus() != 200) {
            ctx.status(HttpStatus.valueOf("ERROR! Oops, it looks like you selected an invalid province :( Try again!"));
            ctx.redirect(PROVINCES_PAGE); // Redirect back to the provinces page
            return;
        }

        try {
            ArrayList<Town> towns = getJsonListFromResponse(response);
            if (towns.isEmpty()) {
                ctx.status(HttpStatus.valueOf("ERROR! No towns found for the selected province."));
                ctx.redirect(PROVINCES_PAGE); // Redirect back to the provinces page
                return;
            }

            Collections.sort(towns, Comparator.comparing(Town::getName));

            // Add the province and towns to the model for rendering
            Map<String, Object> model = Map.of("towns", towns, "province", selectedProvince);
            ctx.render("towns_in_province.html", model);
        } catch (JSONException e) {
            logger.error("Error parsing towns response", e);
            ctx.status(500).result("Unable to load towns for the selected province.");
        }
    };


    public static Handler view_stage = ctx -> {
        // Get the selected province and town from the form parameters
        String selectedProvince = ctx.formParamAsClass("province", String.class)
                .check(Objects::nonNull, "Province was not specified")
                .get();

        String selectedTown = ctx.formParamAsClass("towns", String.class)
                .check(Objects::nonNull, "Town was not specified")
                .get();

        // Fetch the current stage information for the selected town
        HttpResponse<JsonNode> response = Unirest.get(stage_url + selectedTown + "/stage").asJson();

        // If the response is empty or invalid, handle the error
        if (response.getStatus() != 200) {
            ctx.status(HttpStatus.valueOf("ERROR! Oops, it looks like you selected an invalid town or stage data. Please try again!"));
            ctx.redirect(PROVINCES_PAGE); // Redirect back to the provinces page
            return;
        }

        try {
            // Assuming the response contains the current stage in a JSON format
            int currentStage = response.getBody().getObject().getInt("stage");

            // Add the selected town, province, and stage information to the model
            Map<String, Object> model = Map.of(
                    "town", selectedTown,
                    "province", selectedProvince,
                    "currentStage", currentStage
            );

            // Render the 'stage' page with the relevant data
            ctx.render("town_stage.html", model);

        } catch (JSONException e) {
            logger.error("Error parsing stage response", e);
            ctx.status(500).result("Unable to load stage information for the selected town.");
        }
    };




    public static Handler schedule = ctx -> {
        String searchedTown = ctx.queryParam("selectedTown");
        String searchedProvince = ctx.queryParam("selectedProvince");
        int currentStage = getStageFromResponse(Unirest.get(stage_url+"stage").asJson());

        HttpResponse<ScheduleDO> response = Unirest
                .get(schedule_url+searchedProvince+"/"+searchedTown+"/"+currentStage)
                .asObject(ScheduleDO.class);

        // JSONObject scheduleObject = new JSONObject(response.getBody());

        Map<String, Object> model = Map.of("town", searchedTown, "schedule", response.getBody(), "stage", currentStage);
        ctx.render("schedule.html", model);
    };


    // HELPER FUNCTIONS
    private static int getStageFromResponse( HttpResponse<JsonNode> response ) throws JSONException{
        return response.getBody().getObject().getInt( "stage" );
    }
    // private static ScheduleDO getScheduleFromResponse( HttpResponse<JsonNode> response ) throws JSONException{

    // }
    private static ArrayList<String> getListFromResponse(HttpResponse<JsonNode> response) throws JSONException {
        JSONArray jsonArray = new JSONArray(response.getBody().toString());
        ArrayList<String> resultList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.get(i);
            resultList.add((String) element);
        }

        return resultList;
    }
    private static ArrayList<Town> getJsonListFromResponse(HttpResponse<JsonNode> response) throws JSONException {
        JSONArray jsonArray = new JSONArray(response.getBody().toString());
        ArrayList<Town> resultList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject element = jsonArray.getJSONObject(i);

            resultList.add(new Town(element.get("name").toString(), element.get("province").toString()));
        }

        return resultList;
    }
    private static void initJsonMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS );
        Unirest.config().setObjectMapper(new JacksonObjectMapper( mapper ));
    }

}
