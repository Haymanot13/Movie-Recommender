package com.example.moviesrecommendation;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class HelloController {

    @FXML private StackPane mainStackPane;
    @FXML private BorderPane rootPane;
    @FXML private FlowPane moviesBox;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox welcomeBox;
    @FXML private TextField searchField;
    @FXML private ImageView backgroundImageView;
    @FXML private Rectangle backgroundOverlay;
    @FXML private ToggleButton moviesToggle;
    @FXML private ToggleButton seriesToggle;
    @FXML private VBox genreContainer;
    @FXML private VBox yearContainer;
    @FXML private HBox titleBar;
    @FXML private MenuButton culturalSpotlightsMenu;

    private MovieApi movieApi;
    private HostServices hostServices;
    private Parent vibeFinderView;
    private ContextMenu searchSuggestionsPopup;
    private PauseTransition searchDebounce;
    private Timeline backgroundSlideshow;
    private List<Image> backgroundImages = new ArrayList<>();
    private int currentBgImageIndex = 0;
    private ToggleGroup mediaToggleGroup;
    private String lastGenreId = "28";
    private final Random random = new Random();
    private double xOffset = 0;
    private double yOffset = 0;

    private static final Map<String, String> COUNTRY_TO_LANG = new HashMap<>();
    static {
        COUNTRY_TO_LANG.put("japan", "ja"); COUNTRY_TO_LANG.put("japanese", "ja");
        COUNTRY_TO_LANG.put("korea", "ko"); COUNTRY_TO_LANG.put("korean", "ko");
        COUNTRY_TO_LANG.put("china", "zh"); COUNTRY_TO_LANG.put("chinese", "zh");
        COUNTRY_TO_LANG.put("india", "hi"); COUNTRY_TO_LANG.put("indian", "hi"); COUNTRY_TO_LANG.put("hindi", "hi");
        COUNTRY_TO_LANG.put("thailand", "th"); COUNTRY_TO_LANG.put("thai", "th");
        COUNTRY_TO_LANG.put("france", "fr"); COUNTRY_TO_LANG.put("french", "fr");
        COUNTRY_TO_LANG.put("germany", "de"); COUNTRY_TO_LANG.put("german", "de");
        COUNTRY_TO_LANG.put("italy", "it"); COUNTRY_TO_LANG.put("italian", "it");
        COUNTRY_TO_LANG.put("spain", "es"); COUNTRY_TO_LANG.put("spanish", "es");
        COUNTRY_TO_LANG.put("russia", "ru"); COUNTRY_TO_LANG.put("russian", "ru");
        COUNTRY_TO_LANG.put("uk", "en"); COUNTRY_TO_LANG.put("england", "en");
        COUNTRY_TO_LANG.put("usa", "en"); COUNTRY_TO_LANG.put("america", "en");
        COUNTRY_TO_LANG.put("mexico", "es");
        COUNTRY_TO_LANG.put("brazil", "pt"); COUNTRY_TO_LANG.put("portuguese", "pt");
        COUNTRY_TO_LANG.put("turkey", "tr"); COUNTRY_TO_LANG.put("turkish", "tr");
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    public void initialize() {
        scrollPane.setVisible(false);
        welcomeBox.setVisible(true);

        backgroundImageView.fitWidthProperty().bind(mainStackPane.widthProperty());
        backgroundImageView.fitHeightProperty().bind(mainStackPane.heightProperty());
        backgroundOverlay.widthProperty().bind(mainStackPane.widthProperty());
        backgroundOverlay.heightProperty().bind(mainStackPane.heightProperty());

        mediaToggleGroup = new ToggleGroup();
        moviesToggle.setToggleGroup(mediaToggleGroup);
        seriesToggle.setToggleGroup(mediaToggleGroup);
        moviesToggle.setUserData("movie");
        seriesToggle.setUserData("tv");
        
        mediaToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                onPopularClicked();
            }
        });
        
        populateGenres();
        populateYears();
        populateCulturalSpotlights();

        searchSuggestionsPopup = new ContextMenu();
        searchSuggestionsPopup.getStyleClass().add("search-popup");

        searchDebounce = new PauseTransition(Duration.millis(350));
        searchDebounce.setOnFinished(event -> fetchAutocompleteSuggestions(searchField.getText()));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty() || isSpecialCommand(newValue)) {
                searchSuggestionsPopup.hide();
                return;
            }
            searchDebounce.playFromStart();
        });

        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    public void connectToServer(String serverIp) {
        new Thread(() -> {
            try {
                Registry registry = LocateRegistry.getRegistry(serverIp, 1099);
                movieApi = (MovieApi) registry.lookup("MovieApi");
                Platform.runLater(this::initializeBackgroundSlideshow);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    welcomeBox.getChildren().clear();
                    Label errorLabel = new Label("Error: Could not connect to the movie server at " + serverIp);
                    errorLabel.getStyleClass().add("welcome-text");
                    welcomeBox.getChildren().add(errorLabel);
                });
            }
        }).start();
    }
    
    private void populateGenres() {
        String[] genres = {
            "Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary", 
            "Drama", "Family", "Horror", "Romance", "Thriller", "War",
            "Action & Romance", "Romance & Thriller", "Family & Comedy"
        };
        
        for (String genre : genres) {
            Button btn = new Button(genre);
            btn.getStyleClass().add("sidebar-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> onGenreClicked(genre));
            genreContainer.getChildren().add(btn);
        }
    }
    
    private void populateYears() {
        int currentYear = Year.now().getValue();
        for (int i = 0; i < 20; i++) {
            int year = currentYear - i;
            Button btn = new Button(String.valueOf(year));
            btn.getStyleClass().add("sidebar-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> onYearClicked(year));
            yearContainer.getChildren().add(btn);
        }
    }

    private void populateCulturalSpotlights() {
        Map<String, String> spotlights = new LinkedHashMap<>();
        spotlights.put("🇪🇹 Ethiopian", "ET");
        spotlights.put("🇮🇳 Indian", "IN");
        spotlights.put("🇰🇷 Korean", "KR");
        spotlights.put("🇯🇵 Japanese", "JP");
        spotlights.put("🇫🇷 French", "FR");
        spotlights.put("🇪🇸 Spanish", "ES");

        for (Map.Entry<String, String> entry : spotlights.entrySet()) {
            MenuItem item = new MenuItem(entry.getKey());
            item.setOnAction(e -> loadItems(() -> movieApi.getItemsByCountry(getCurrentMediaType(), entry.getValue())));
            culturalSpotlightsMenu.getItems().add(item);
        }
    }

    private void initializeBackgroundSlideshow() {
        new Thread(() -> {
            try {
                if (movieApi == null) return;
                List<Movie> trendingMovies = movieApi.getTrendingItems("movie");
                backgroundImages = trendingMovies.stream()
                    .map(movie -> new Image("https://image.tmdb.org/t/p/w1280" + movie.getPosterPath(), true))
                    .collect(Collectors.toList());

                if (backgroundImages.isEmpty()) return;

                Platform.runLater(() -> {
                    backgroundImageView.setImage(backgroundImages.get(0));
                    
                    backgroundSlideshow = new Timeline(new KeyFrame(Duration.seconds(7), event -> {
                        currentBgImageIndex = (currentBgImageIndex + 1) % backgroundImages.size();
                        FadeTransition ft = new FadeTransition(Duration.millis(1500), backgroundImageView);
                        ft.setFromValue(0.5);
                        ft.setToValue(1.0);
                        ft.setOnFinished(e -> {
                            backgroundImageView.setImage(backgroundImages.get(currentBgImageIndex));
                            FadeTransition ft2 = new FadeTransition(Duration.millis(1500), backgroundImageView);
                            ft2.setFromValue(1.0);
                            ft2.setToValue(0.5);
                            ft2.play();
                        });
                        ft.play();
                    }));
                    backgroundSlideshow.setCycleCount(Timeline.INDEFINITE);
                    backgroundSlideshow.play();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private String getCurrentMediaType() {
        if (mediaToggleGroup.getSelectedToggle() == null) return "movie";
        return (String) mediaToggleGroup.getSelectedToggle().getUserData();
    }

    @FXML
    private void onPopularClicked() {
        loadItems(() -> movieApi.getTrendingItems(getCurrentMediaType()));
    }

    @FXML
    private void onRecentClicked() {
        loadItems(() -> movieApi.getRecentItems(getCurrentMediaType()));
    }

    @FXML
    private void onOldClicked() {
        loadItems(() -> movieApi.getOldItems(getCurrentMediaType()));
    }
    
    private void onGenreClicked(String genreName) {
        String genreIds = getGenreId(genreName);
        lastGenreId = genreIds.split(",")[0];
        loadItems(() -> movieApi.getItemsByGenre(getCurrentMediaType(), genreIds));
    }
    
    private void onYearClicked(int year) {
        loadItems(() -> movieApi.getItemsByYear(getCurrentMediaType(), year));
    }

    private interface ItemLoader {
        List<Movie> load() throws Exception;
    }

    private void loadItems(ItemLoader loader) {
        if (movieApi == null) {
            Platform.runLater(() -> {
                welcomeBox.getChildren().clear();
                Label errorLabel = new Label("Not connected to server.");
                errorLabel.getStyleClass().add("welcome-text");
                welcomeBox.getChildren().add(errorLabel);
            });
            return;
        }

        if (rootPane.getCenter() != scrollPane) {
            rootPane.setCenter(scrollPane);
        }
        scrollPane.setVisible(true);
        welcomeBox.setVisible(false);
        moviesBox.getChildren().clear();

        Label loadingLabel = new Label("Loading...");
        loadingLabel.getStyleClass().add("quiz-title");
        moviesBox.getChildren().add(loadingLabel);

        new Thread(() -> {
            try {
                final List<Movie> items = loader.load();
                Platform.runLater(() -> {
                    moviesBox.getChildren().clear();
                    if (items.isEmpty()) {
                        Label noResults = new Label("No items found.");
                        noResults.getStyleClass().add("quiz-title");
                        moviesBox.getChildren().add(noResults);
                    } else {
                        for (Movie item : items) {
                            VBox card = createMovieCard(item);
                            moviesBox.getChildren().add(card);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    moviesBox.getChildren().clear();
                    Label errorLabel = new Label("Oops! Could not load items.");
                    errorLabel.getStyleClass().add("quiz-title");
                    moviesBox.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    private boolean isSpecialCommand(String query) {
        String lowerQuery = query.trim().toLowerCase();
        return lowerQuery.equals("trending") || lowerQuery.equals("top rated") || COUNTRY_TO_LANG.containsKey(lowerQuery);
    }

    private void fetchAutocompleteSuggestions(String query) {
        if (query.trim().isEmpty() || movieApi == null) return;

        new Thread(() -> {
            try {
                List<Movie> suggestions = movieApi.searchItemsByTitle(getCurrentMediaType(), query);
                Platform.runLater(() -> {
                    searchSuggestionsPopup.getItems().clear();
                    if (suggestions.isEmpty()) {
                        searchSuggestionsPopup.hide();
                        return;
                    }
                    suggestions.stream().limit(7).forEach(movie -> {
                        MenuItem item = new MenuItem(movie.getTitle());
                        item.setOnAction(e -> {
                            searchField.setText(movie.getTitle());
                            searchSuggestionsPopup.hide();
                            onSearch();
                        });
                        searchSuggestionsPopup.getItems().add(item);
                    });
                    if (!searchSuggestionsPopup.isShowing()) {
                        searchSuggestionsPopup.show(searchField, Side.BOTTOM, 0, 0);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void onSearch() {
        searchSuggestionsPopup.hide();
        String query = searchField.getText().trim();
        String lowerQuery = query.toLowerCase();
        String mediaType = getCurrentMediaType();

        loadItems(() -> {
            if (lowerQuery.equals("trending")) {
                return movieApi.getTrendingItems(mediaType);
            } else if (lowerQuery.equals("top rated")) {
                return movieApi.getTopRatedItems(mediaType);
            } else if (COUNTRY_TO_LANG.containsKey(lowerQuery)) {
                String langCode = COUNTRY_TO_LANG.get(lowerQuery);
                return movieApi.getItemsByCountry(mediaType, langCode.toUpperCase());
            } else {
                return movieApi.searchItemsByTitle(mediaType, query);
            }
        });
    }

    @FXML
    private void onVibeFinderClicked() {
        if (vibeFinderView == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("vibefinder-view.fxml"));
                vibeFinderView = loader.load();
                VibeFinderController controller = loader.getController();
                controller.setOnQuizFinished(this::handleVibeSelection);
                controller.setOnBack(() -> {
                    rootPane.setCenter(scrollPane);
                    onPopularClicked();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        rootPane.setCenter(vibeFinderView);
        scrollPane.setVisible(false);
        welcomeBox.setVisible(false);
    }

    private void handleVibeSelection(String selection) {
        rootPane.setCenter(scrollPane);
        ItemLoader loader = () -> {
            String mediaType = getCurrentMediaType();
            switch (selection) {
                case "happy": return movieApi.getVibeMovies(mediaType, "35");
                case "sad": return movieApi.getVibeMovies(mediaType, "18");
                case "romantic": return movieApi.getVibeMovies(mediaType, "10749");
                case "angry": return movieApi.getVibeMovies(mediaType, "28,53");
                case "tired": return movieApi.getVibeMovies(mediaType, "16,10751");
                case "motivated": return movieApi.getVibeMovies(mediaType, "99");
                case "30-60": return movieApi.getItemsByRuntime(mediaType, 30, 60);
                case "60-120": return movieApi.getItemsByRuntime(mediaType, 60, 120);
                case "120+": return movieApi.getItemsByRuntime(mediaType, 120, 999);
                case "happy_ending": return movieApi.getVibeMovies(mediaType, "35,10749");
                case "sad_ending": return movieApi.getVibeMovies(mediaType, "18");
                case "twist_ending": return movieApi.getVibeMovies(mediaType, "9648,53");
                case "open_ending": return movieApi.getVibeMovies(mediaType, "878");
                default: return movieApi.getTrendingItems(mediaType);
            }
        };
        
        loadItems(() -> loader.load().stream().limit(3).collect(Collectors.toList()));
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(5);
        card.getStyleClass().add("movie-card");
        card.setPrefWidth(220);

        ImageView poster = new ImageView();
        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            Image image = new Image("https://image.tmdb.org/t/p/w200" + movie.getPosterPath(), true);
            poster.setImage(image);
        }
        poster.setFitWidth(220);
        poster.setPreserveRatio(true);
        poster.getStyleClass().add("poster-image");

        Label title = new Label(movie.getTitle());
        title.getStyleClass().add("movie-title");
        title.setWrapText(true);

        String year = (movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4) 
                      ? movie.getReleaseDate().substring(0, 4) : "N/A";
        String lang = (movie.getOriginalLanguage() != null) ? movie.getOriginalLanguage().toUpperCase() : "EN";
        String rating = String.format("%.1f", movie.getVoteAverage());

        Label metaLabel = new Label(year + " • " + lang + " • ★ " + rating);
        metaLabel.getStyleClass().add("movie-meta");

        Label overview = new Label(movie.getOverview());
        overview.getStyleClass().add("movie-overview");
        overview.setWrapText(true);
        overview.setMaxHeight(60);

        Button watchButton = new Button("Watch Trailer");
        watchButton.getStyleClass().add("action-button");
        watchButton.setOnAction(e -> watchTrailer(movie));

        Button downloadButton = new Button("Download Trailer");
        downloadButton.getStyleClass().add("action-button");
        downloadButton.setTooltip(new Tooltip("Opens an external download site"));
        downloadButton.setOnAction(e -> downloadTrailer(movie));

        HBox buttonBox = new HBox(5, watchButton, downloadButton);
        buttonBox.getStyleClass().add("button-box");

        card.getChildren().addAll(poster, title, metaLabel, overview, buttonBox);
        return card;
    }

    private void watchTrailer(Movie movie) {
        new Thread(() -> {
            try {
                String mediaType = movie.getMediaType();
                if (mediaType == null) mediaType = getCurrentMediaType();
                String trailerUrl = movieApi.getTrailerUrl(movie.getId(), mediaType);
                
                if (trailerUrl != null && !trailerUrl.contains("search_query")) {
                    Platform.runLater(() -> {
                        if (hostServices != null) {
                            hostServices.showDocument(trailerUrl);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void downloadTrailer(Movie movie) {
        new Thread(() -> {
            try {
                String mediaType = movie.getMediaType();
                if (mediaType == null) mediaType = getCurrentMediaType();
                String trailerUrl = movieApi.getTrailerUrl(movie.getId(), mediaType);
                
                if (trailerUrl != null && !trailerUrl.contains("search_query")) {
                    String downloadUrl = trailerUrl.replace("youtube.com", "ssyoutube.com");
                    Platform.runLater(() -> {
                        if (hostServices != null) {
                            hostServices.showDocument(downloadUrl);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String getGenreId(String genreName) {
        return switch (genreName) {
            case "Action" -> "28";
            case "Adventure" -> "12";
            case "Animation" -> "16";
            case "Comedy" -> "35";
            case "Crime" -> "80";
            case "Documentary" -> "99";
            case "Drama" -> "18";
            case "Family" -> "10751";
            case "Horror" -> "27";
            case "Romance" -> "10749";
            case "Thriller" -> "53";
            case "War" -> "10752";
            case "Action & Romance" -> "28,10749";
            case "Romance & Thriller" -> "10749,53";
            case "Family & Comedy" -> "10751,35";
            default -> "28";
        };
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onMinimize() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void onMaximize() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }
}