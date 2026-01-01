package com.example.moviesrecommendation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MovieService {
    private static final String API_KEY = "";
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    private final Client client = ClientBuilder.newClient();
    private final Gson gson = new Gson();
    private final Random random = new Random();

    private List<Movie> getMoviesFrom(WebTarget target, String mediaType) {
        try {
            String jsonResponse = target.request(MediaType.APPLICATION_JSON).get(String.class);
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            Type movieListType = new TypeToken<List<Movie>>() {}.getType();
            List<Movie> movies = gson.fromJson(jsonObject.get("results"), movieListType);
            if (movies != null) {
                movies.forEach(m -> m.setMediaType(mediaType));
                return movies;
            }
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Movie> getItemsByGenre(String mediaType, String genreIds) {
        WebTarget target = client.target(BASE_URL)
                .path("/discover/" + mediaType)
                .queryParam("api_key", API_KEY)
                .queryParam("with_genres", genreIds);
        return getMoviesFrom(target, mediaType);
    }

    public List<Movie> getVibeMovies(String mediaType, String genreIds) {
        WebTarget target = client.target(BASE_URL)
                .path("/discover/" + mediaType)
                .queryParam("api_key", API_KEY)
                .queryParam("with_genres", genreIds)
                .queryParam("sort_by", "vote_average.desc")
                .queryParam("vote_count.gte", 500);
        return getMoviesFrom(target, mediaType);
    }

    public List<Movie> getItemsByRuntime(String mediaType, int gte, int lte) {
        WebTarget target = client.target(BASE_URL)
                .path("/discover/" + mediaType)
                .queryParam("api_key", API_KEY)
                .queryParam("sort_by", "popularity.desc")
                .queryParam("with_runtime.gte", gte)
                .queryParam("with_runtime.lte", lte);
        return getMoviesFrom(target, mediaType);
    }

    public List<Movie> searchItemsByTitle(String mediaType, String query) {
        WebTarget target = client.target(BASE_URL)
                .path("/search/" + mediaType)
                .queryParam("api_key", API_KEY)
                .queryParam("query", query);
        return getMoviesFrom(target, mediaType);
    }

    public List<Movie> getTrendingItems(String mediaType) {
        WebTarget target = client.target(BASE_URL)
                .path("/trending/" + mediaType + "/week")
                .queryParam("api_key", API_KEY);
        return getMoviesFrom(target, mediaType);
    }

    public List<Movie> getTopRatedItems(String mediaType) {
        WebTarget target = client.target(BASE_URL)
                .path("/" + mediaType + "/top_rated")
                .queryParam("api_key", API_KEY);
        return getMoviesFrom(target, mediaType);
    }

    public List<Movie> getItemsByCountry(String mediaType, String countryCode) {
        WebTarget target = client.target(BASE_URL)
                .path("/discover/" + mediaType)
                .queryParam("api_key", API_KEY)
                .queryParam("with_origin_country", countryCode)
                .queryParam("sort_by", "popularity.desc");
        return getMoviesFrom(target, mediaType);
    }

    public List<Movie> getItemsByYear(String mediaType, int year) {
        WebTarget target = client.target(BASE_URL)
                .path("/discover/" + mediaType)
                .queryParam("api_key", API_KEY)
                .queryParam(mediaType.equals("movie") ? "primary_release_year" : "first_air_date_year", year);
        return getMoviesFrom(target, mediaType);
    }

    public List<Movie> getRecentItems(String mediaType) {
        WebTarget target = client.target(BASE_URL)
                .path("/" + mediaType + "/" + (mediaType.equals("movie") ? "now_playing" : "on_the_air"))
                .queryParam("api_key", API_KEY);
        return getMoviesFrom(target, mediaType);
    }

    public List<Movie> getOldItems(String mediaType) {
        WebTarget target = client.target(BASE_URL)
                .path("/discover/" + mediaType)
                .queryParam("api_key", API_KEY)
                .queryParam("sort_by", "popularity.desc")
                .queryParam(mediaType.equals("movie") ? "primary_release_date.lte" : "first_air_date.lte", "1999-12-31");
        return getMoviesFrom(target, mediaType);
    }

    public List<Movie> getRandomItems(String mediaType) {
        int randomPage = random.nextInt(50) + 1;
        WebTarget target = client.target(BASE_URL)
                .path("/" + mediaType + "/top_rated")
                .queryParam("api_key", API_KEY)
                .queryParam("page", randomPage);
        
        List<Movie> movies = getMoviesFrom(target, mediaType);
        Collections.shuffle(movies);
        return movies.stream().limit(1).collect(Collectors.toList());
    }

    public String getTrailerUrl(int itemId, String mediaType) {
        WebTarget target = client.target(BASE_URL)
                .path("/" + mediaType + "/" + itemId + "/videos")
                .queryParam("api_key", API_KEY);

        String jsonResponse = target.request(MediaType.APPLICATION_JSON).get(String.class);
        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
        JsonArray results = jsonObject.getAsJsonArray("results");

        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                JsonObject video = results.get(i).getAsJsonObject();
                String site = video.get("site").getAsString();
                String type = video.get("type").getAsString();
                if ("YouTube".equalsIgnoreCase(site) && "Trailer".equalsIgnoreCase(type)) {
                    return YOUTUBE_URL + video.get("key").getAsString();
                }
            }
        }
        return "https://www.youtube.com/results?search_query=" + (mediaType.equals("movie") ? "movie" : "tv show") + " trailer " + itemId;
    }
}