/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pe.edu.pucp.tesis.model;

import pe.edu.pucp.tesis.config.ConfigAlgoritmo;
import pe.edu.pucp.tesis.config.ConfigMedico;

/**
 *
 * @author PC-HP
 */
public class Medico {
    
    String nombre;
    String cmp; //Codigo de colegio médico
    int experiencia; //Años de experiencia
    int turnosTrabajo; //Turnos de trabajo del médico
    int turnosDisponibles; //Turnos disponibles del médico
    int especialidad;
    int posMatriz;
    double bondad;
    boolean enListaRCL;
    int turnosDisponiblesSemana;
    int tiempoProximaCita;

    public Medico()
    {
        bondad=0;
        enListaRCL=false;
    }

    public int getTiempoProximaCita() {
        return tiempoProximaCita;
    }

    public void setTiempoProximaCita(int tiempoProximaCita) {
        this.tiempoProximaCita = tiempoProximaCita;
    }

    public Medico(Medico m) {
        nombre=m.getNombre();
        cmp=m.getCmp();
        experiencia=m.getExperiencia();
        System.out.println("TT: "+m.getTurnosTrabajo());
        turnosTrabajo=m.getTurnosTrabajo();
        System.out.println("TD: "+m.getTurnosDisponibles());
        turnosDisponibles=m.getTurnosDisponibles();
        especialidad=m.getEspecialidad();
        posMatriz=m.getPosMatriz();
        bondad=0;
        enListaRCL=false;
        System.out.println("TDS: "+m.getTurnosDisponiblesSemana());
        turnosDisponiblesSemana=m.getTurnosDisponiblesSemana();
    }

    public int getTurnosDisponiblesSemana() {
        return turnosDisponiblesSemana;
    }

    public void setTurnosDisponiblesSemana(int turnosDisponiblesSemana) {
        this.turnosDisponiblesSemana = turnosDisponiblesSemana;
    }
    
    public boolean isEnListaRCL() {
        return enListaRCL;
    }

    public void setEnListaRCL(boolean enListaPendiente) {
        this.enListaRCL = enListaPendiente;
    }

    public double getBondad(int [][] MT,int []S) 
    {
        double tiempoCita=0;
//        if (change)
//        {
            //Calculamos el tiempo de proxima cita
            for (int i=0;i<7;i++)
            {
                if (MT[i][posMatriz]!=0)
                {
                    //semana trabajo * numero de dias
                    tiempoCita+=7*(S[posMatriz]-1)+i+1;
                    break;
                }
            }
            if (tiempoCita==0)
            {
                System.out.println("TDisp: "+this.getTurnosDisponibles());
                System.out.println("TDispSem: "+this.getTurnosDisponiblesSemana());
                
            }
            this.bondad=((ConfigMedico.vExperiencia)*experiencia*(ConfigMedico.vFactor)*((double)turnosDisponibles/turnosTrabajo))
                       /(ConfigMedico.vTiempoCita*tiempoCita);
            
   //        }
//        if (bondad==0) System.out.println("??????????????");
//        System.out.println("E: "+experiencia+" TD:"+turnosDisponibles+" TT:"+turnosTrabajo+" Tiempo: "+tiempoCita);
//        System.out.println("B: "+bondad);
//        System.out.println("");
        return bondad;
    }

    public void setBondad(double bondad) {
        this.bondad = bondad;
    }

    
    public int getPosMatriz() {
        return posMatriz;
    }

    public void setPosMatriz(int posMatriz) {
        this.posMatriz = posMatriz;
    }
    
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCmp() {
        return cmp;
    }

    public void setCmp(String cmp) {
        this.cmp = cmp;
    }

    public int getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(int experiencia) {
        this.experiencia = experiencia;
    }

    public int getTurnosTrabajo() {
        return turnosTrabajo;
    }

    public void setTurnosTrabajo(int turnosTrabajo) {
        this.turnosTrabajo = turnosTrabajo;
        this.turnosDisponibles=turnosTrabajo;
//        System.out.println(turnosDisponibles);
        this.turnosDisponiblesSemana=this.turnosTrabajo/ConfigAlgoritmo.N_SEMANAS_TRABAJO;
    }

    public int getTurnosDisponibles() {
        return turnosDisponibles;
    }

    public void setTurnosDisponibles(int turnosDisponibles) {
        this.turnosDisponibles = turnosDisponibles;
    }

    public int getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(int especialidad) {
        this.especialidad = especialidad;
    }
    
    public void reducirCitasDisponibles(int [][]MT)
    {
//        System.out.println("Reducido "+this.cmp +" - "+this.turnosDisponibles);
        this.turnosDisponibles--;
        this.turnosDisponiblesSemana--;
        for (int i=0;i<7;i++)
        {
            if (MT[i][posMatriz]!=0)
            {
                //semana trabajo * numero de dias
                MT[i][posMatriz]--;
                break;
            }
        }
    }
}
