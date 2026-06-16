package br.com.statezone.integration.apifootball.dto;

public record PlayerDto(
        Long id,
        String name,
        Integer age,
        Integer number,
        String position,
        String photo
) {}
