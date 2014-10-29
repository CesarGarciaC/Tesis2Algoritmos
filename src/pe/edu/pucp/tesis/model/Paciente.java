/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pe.edu.pucp.tesis.model;

import pe.edu.pucp.tesis.config.ConfigPaciente;

/**
 *
 * @author PC-HP
 */
public class Paciente {
    
    String nombre;
    double edad;
    double costoEnfDx;
    int especialidadReq;
    int cargaFamiliar;
    int numCitasUltimoAnio;
    double ingresoMensual;
    double bondad;
    int posMatriz;
    Medico medicoTratante;
    boolean enListaRCL;
    double bondadMedicoAsignado;
    
    public Paciente()
    {
        bondad=0;
        medicoTratante=null;
    }

    public double getBondadMedicoAsignado() {
        return bondadMedicoAsignado;
    }

    public void setBondadMedicoAsignado(double bondadMedicoAsignado) {
        this.bondadMedicoAsignado = bondadMedicoAsignado;
    }
    
    public boolean isEnListaRCL() {
        return enListaRCL;
    }

    public void setEnListaRCL(boolean enListaRCL) {
        this.enListaRCL = enListaRCL;
    }
    
    public Paciente(Paciente p) {
        nombre=p.getNombre();
        medicoTratante=p.getMedicoTratante();
        edad=p.getEdad();
        costoEnfDx=p.getCostoEnfDx();
        especialidadReq=p.getEspecialidadReq();
        cargaFamiliar=p.getCargaFamiliar();
        numCitasUltimoAnio=p.getNumCitasUltimoAnio();
        ingresoMensual=p.getIngresoMensual();
        posMatriz=p.getPosMatriz();
        bondad=0;
    }

    public Medico getMedicoTratante() {
        return medicoTratante;
    }

    public void setMedicoTratante(Medico medicoTratante) {
        this.medicoTratante = medicoTratante;
    }

    public int getPosMatriz() {
        return posMatriz;
    }

    public void setPosMatriz(int posMatriz) {
        this.posMatriz = posMatriz;
    }

    public double getEdad() {
        return edad;
    }

    public void setEdad(double edad) {
        this.edad = edad;
    }

    public int getCargaFamiliar() {
        return cargaFamiliar;
    }

    public void setCargaFamiliar(int cargaFamiliar) {
        this.cargaFamiliar = cargaFamiliar;
    }

    public int getNumCitasUltimoAnio() {
        return numCitasUltimoAnio;
    }

    public void setNumCitasUltimoAnio(int numCitasUltimoAnio) {
        this.numCitasUltimoAnio = numCitasUltimoAnio;
    }
    
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getCostoEnfDx() {
        return costoEnfDx;
    }

    public void setCostoEnfDx(double costoEnfDx) {
        this.costoEnfDx = costoEnfDx;
    }

    public int getEspecialidadReq() {
        return especialidadReq;
    }

    public void setEspecialidadReq(int especialidadReq) {
        this.especialidadReq = especialidadReq;
    }

    public int getNumHijos() {
        return cargaFamiliar;
    }

    public void setNumHijos(int numHijos) {
        this.cargaFamiliar = numHijos;
    }

    public double getIngresoMensual() {
        return ingresoMensual;
    }

    public void setIngresoMensual(double ingresoMensual) {
        this.ingresoMensual = ingresoMensual;
    }

    public double getBondad() {
        
        if (bondad==0)
        {            
            bondad=(ConfigPaciente.vEdad*getFuncionEdad() * ConfigPaciente.vCostoEnf*costoEnfDx*
                    ConfigPaciente.vCargaFamiliar*(cargaFamiliar+1) * ConfigPaciente.vNumCitas*(numCitasUltimoAnio+1))
                    /(ConfigPaciente.vIngreso*ingresoMensual);
        }
        return bondad;
    }
    
    public double getFuncionEdad()
    {
        double n=0.015*(edad*edad) - edad + 20;
        return n;
    }

    public void setBondad(double bondad) {
        this.bondad = bondad;
    }
}
