package br.ufpr.pid.hc.dto;

import lombok.Value;

import java.io.Serializable;

@Value
public class Alteracao implements Serializable {
    String campo;
    String antes;
    String depois;
}
