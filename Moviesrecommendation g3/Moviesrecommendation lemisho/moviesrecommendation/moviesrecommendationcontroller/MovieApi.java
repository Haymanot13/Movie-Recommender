package com.example.moviesrecommendation;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MovieApi extends Remote {
    List<Movie> getItemsByGenre(String mediaType, String genreIds) throws RemoteException;
    List<Movie> getVibeMovies(String mediaType, String genreIds) throws RemoteException;
    List<Movie> getItemsByRuntime(String mediaType, int gte, int lte) throws RemoteException;
    List<Movie> searchItemsByTitle(String mediaType, String query) throws RemoteException;
    List<Movie> getTrendingItems(String mediaType) throws RemoteException;
    List<Movie> getTopRatedItems(String mediaType) throws RemoteException;
    List<Movie> getItemsByCountry(String mediaType, String countryCode) throws RemoteException;
    List<Movie> getItemsByYear(String mediaType, int year) throws RemoteException;
    List<Movie> getRecentItems(String mediaType) throws RemoteException;
    List<Movie> getOldItems(String mediaType) throws RemoteException;
    List<Movie> getRandomItems(String mediaType) throws RemoteException;
    String getTrailerUrl(int itemId, String mediaType) throws RemoteException;
}