package com.example.moviesrecommendation;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class MovieApiServer extends UnicastRemoteObject implements MovieApi {
    private final transient MovieService movieService = new MovieService();

    public MovieApiServer() throws RemoteException {
        super();
    }

    @Override
    public List<Movie> getItemsByGenre(String mediaType, String genreIds) {
        return movieService.getItemsByGenre(mediaType, genreIds);
    }

    @Override
    public List<Movie> getVibeMovies(String mediaType, String genreIds) {
        return movieService.getVibeMovies(mediaType, genreIds);
    }

    @Override
    public List<Movie> getItemsByRuntime(String mediaType, int gte, int lte) {
        return movieService.getItemsByRuntime(mediaType, gte, lte);
    }

    @Override
    public List<Movie> searchItemsByTitle(String mediaType, String query) {
        return movieService.searchItemsByTitle(mediaType, query);
    }

    @Override
    public List<Movie> getTrendingItems(String mediaType) {
        return movieService.getTrendingItems(mediaType);
    }

    @Override
    public List<Movie> getTopRatedItems(String mediaType) {
        return movieService.getTopRatedItems(mediaType);
    }

    @Override
    public List<Movie> getItemsByCountry(String mediaType, String countryCode) {
        return movieService.getItemsByCountry(mediaType, countryCode);
    }

    @Override
    public List<Movie> getItemsByYear(String mediaType, int year) {
        return movieService.getItemsByYear(mediaType, year);
    }

    @Override
    public List<Movie> getRecentItems(String mediaType) {
        return movieService.getRecentItems(mediaType);
    }

    @Override
    public List<Movie> getOldItems(String mediaType) {
        return movieService.getOldItems(mediaType);
    }

    @Override
    public List<Movie> getRandomItems(String mediaType) {
        return movieService.getRandomItems(mediaType);
    }

    @Override
    public String getTrailerUrl(int itemId, String mediaType) {
        return movieService.getTrailerUrl(itemId, mediaType);
    }

    public static void main(String[] args) {
        try {
            MovieApiServer server = new MovieApiServer();
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(1099);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(1099);
            }
            registry.rebind("MovieApi", server);
            System.out.println("Server is running...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
