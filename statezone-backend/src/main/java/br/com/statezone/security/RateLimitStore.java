package br.com.statezone.security;

public interface RateLimitStore {

    boolean permitir(String chave);
}
