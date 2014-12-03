/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pe.edu.pucp.tesis.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import pe.edu.pucp.tesis.config.ConfigAlgoritmo;
import pe.edu.pucp.tesis.model.Medico;
import pe.edu.pucp.tesis.model.Paciente;
import pe.edu.pucp.tesis.util.LecturaArchivo;


/**
 *
 * @author PC-HP
 */
public class Voraz {

    public int[][] MA=null,MT=null,MO=null;
    public int []S_V=null;
    public List<Paciente> listaPacientes=new ArrayList<>();
    public List<Medico> listaMedicos=new ArrayList<>();
    public List<Object> listaSol=new ArrayList<>();
    public int[] listaPacientesxPosMedicos;
    public int[] listaOrdenAt;
    public double[] bondadMedicoxPaciente;
    public double fObjetivo;
    public int pacientesAtendidos=0;
    
    public void ejecutarVoraz()
    {
        List<Paciente> listaPendientesP;
        List<Medico> listaPendientesM;
        int [][]MTCopia=duplicarMT();
        
        for (int e=1;e<=ConfigAlgoritmo.N_ESPECIALIDADES;e++)
        {   
            listaPendientesP=obtenerPacientesEspecialidad(e);
            listaPendientesM=obtenerMedicosEspecialidad(e);
            while ((!listaPendientesM.isEmpty()) && (!listaPendientesP.isEmpty()))
            {
                listaPendientesM=ordernarTiempoAtencion(listaPendientesM,MTCopia,S_V);
                Paciente pElegido=listaPendientesP.remove(0);
                System.out.println("Atendiendo: "+pElegido.getNombre());
//                System.out.println("Atendiendo a "+pElegido.getNombre());
                Medico mElegido=obtenerMedicoMenorTiempo(listaPendientesM,pElegido);
//                System.out.println("Atendiendo a: "+pElegido.getNombre());
                //Si no existe medico tratante disponible
                
                if (mElegido==null) continue;
                
                bondadMedicoxPaciente[pElegido.getPosMatriz()]=mElegido.getBondad(MTCopia, S_V);
                pacientesAtendidos++;
//                int tAtencion=7*(S[mElegido.getPosMatriz()]-1) + obtenerDiaSemanaLibre(MTCopia, mElegido);
                
//                MA[pElegido.getPosMatriz()][mElegido.getPosMatriz()]=tAtencion;
                MA[pElegido.getPosMatriz()][mElegido.getPosMatriz()]=mElegido.getTiempoProximaCita();
                listaOrdenAt[mElegido.getPosMatriz()]=listaOrdenAt[mElegido.getPosMatriz()]+1;
                MO[pElegido.getPosMatriz()][mElegido.getPosMatriz()]=listaOrdenAt[mElegido.getPosMatriz()];
                mElegido.reducirCitasDisponibles(MTCopia);
                listaPacientesxPosMedicos[pElegido.getPosMatriz()]=mElegido.getPosMatriz();
//                System.out.println("Medico: "+mElegido.getNombre()+ " "+ mElegido.getTurnosDisponibles());
                if (mElegido.getTurnosDisponiblesSemana()==0)
                {
                    if (S_V[mElegido.getPosMatriz()]==ConfigAlgoritmo.N_SEMANAS_TRABAJO)
                        listaPendientesM.remove(mElegido);
                    else
                    {
//                        System.out.println(mElegido.getTurnosDisponibles()+" / "+S[mElegido.getPosMatriz()]);
                        mElegido.setTurnosDisponibles(mElegido.getTurnosTrabajo());
                        S_V[mElegido.getPosMatriz()]++;
                        ReiniciarMatrizTurnos(MTCopia,mElegido.getPosMatriz());
                    }
                }
                listaPendientesP.remove(pElegido);
            }
        }
        listaPendientesM=null;
        listaPendientesP=null;
        MTCopia=null;
    }
    
    public void ReiniciarMatrizTurnos(int [][]MTCopia,int indexM)
    {
        for (int i=0;i<7;i++)
            MTCopia[i][indexM]=MT[i][indexM];
    }
    
    public List<Paciente> obtenerPacientesEspecialidad(int e)
    {
        List<Paciente> nLista=new ArrayList<>();
        for (Paciente p:listaPacientes)
        {
            if (p.getEspecialidadReq()==e)
                nLista.add(p);
        }
        return nLista;
    }
    
    public List<Medico> obtenerMedicosEspecialidad(int e)
    {
        List<Medico> nLista=new ArrayList<>();
        for (Medico m:listaMedicos)
        {
            if (m.getEspecialidad()==e)
                nLista.add(m);
        }
        return nLista;
    }
    
    public Medico obtenerMedicoMenorTiempo(List<Medico> l, Paciente p)
    {
        Medico resp=null;
        
        if (p.getMedicoTratante()==null)
            return l.get(0);
        
        for (Medico m:l)
        {
            if (p.getMedicoTratante()!=null)
            {
                if (m.getCmp().equalsIgnoreCase(p.getMedicoTratante().getCmp()))
                {
                    resp=m;
                    break;
                }
            }
        }
        return resp;
    }
    
    public List<Medico> ordernarTiempoAtencion(List<Medico> l,int [][]MTCopia, int []S)
    {
        for (Medico m:l)
        {
            int tAtencion=7*(S[m.getPosMatriz()]-1) + obtenerDiaSemanaLibre(MTCopia, m);
            m.setTiempoProximaCita(tAtencion);
        }
        
        Collections.sort(l, new Comparator<Medico>() {

               @Override
               public int compare(Medico o1, Medico o2) {
                   return Double.compare(o1.getTiempoProximaCita(), o2.getTiempoProximaCita());
               }
           });    
        return l;
    }
    
    public boolean todosPendientes(List<Medico> l)
    {
        for (int i=0;i<l.size();i++)
            if (!l.get(i).isEnListaRCL()) return false;
        
        return true;
    }
    
    public int obtenerMinimoMedico(List<Medico> l)
    {
        for (int i=0;i<l.size();i++)
            if (!l.get(i).isEnListaRCL()) return i;
        return -1;
    }
    
    public int obtenerMaximoMedico(List<Medico> l)
    {
        for (int i=l.size()-1;i>=0;i--)
            if (!l.get(i).isEnListaRCL()) return i;
        return -1;
    }
    
    public int[][] duplicarMT()
    {
        int [][]MTCopia=new int[7][ConfigAlgoritmo.N_MEDICOS];
        for (int c=0;c<ConfigAlgoritmo.N_MEDICOS;c++)
            for (int f=0;f<7;f++)
                MTCopia[f][c]=MT[f][c];
        
        return MTCopia;
    }
    
    public int[][]duplicarMatriz(int[][] m,int nf,int nc)
    {
        int [][] nMatriz=new int[nf][nc];
        for (int _nc=0;_nc<nc;_nc++)
            for (int _nf=0;_nf<nf;_nf++)
                nMatriz[_nf][_nc]=m[_nf][_nc];
        
        return nMatriz;
    }
    
    public int[] duplicarLista(int []l,int n)
    {
        int[] nl=new int[n];
        for (int i=0;i<n;i++)
            nl[i]=l[i];
        return nl;
    }
    
    public int obtenerDiaSemanaLibre(int [][]MTCopia, Medico m)
    {
        int indexC=m.getPosMatriz();
        
        for (int i=0;i<=6;i++)
        {
            if (MTCopia[i][indexC]!=0) return i+1;
        }
        System.out.println("????????????????");
        return -1;
    }
    
    public List<Paciente> OrdenarBondadPacientes(List<Paciente> lp,int esp,int f)
    {
        List<Paciente> temp=new ArrayList<>();
        int x=0;
        for (Paciente p: lp)
        {
            if (p.getEspecialidadReq()==esp)
            {
                x++;
                Paciente px;
                if (f==1)
                    px=new Paciente(p);
                else
                    px=p;
                
                if (temp.size()==0) temp.add(px);
                else
                {
                    if (temp.get(temp.size()-1).getBondad()<=px.getBondad()) //Validamos primero con el ultimo elemento
                    {
                        temp.add(px);
                        continue;
                    }
                                        
                    for (int i=0;i<temp.size();i++)
                    {
                      if (temp.get(i).getBondad()>px.getBondad())  
                      {
                        temp.add(i, px); 
                        break;
                      }
                    }
                }
            }
        }
        return temp;
    }
    
    public List<Medico> OrdenarBondadMedicos(List<Medico> lm,int [][]MTCopia,int esp,int f)
    {
        List<Medico> temp=new ArrayList<>();
        int x=0;
        
        if (f==1)
        {
//            long timeStart = System.currentTimeMillis();
            
            for (Medico m: lm)
            {
                if (m.getEspecialidad()==esp)
                {
                    x++;
                    Medico mx=new Medico(m);

                    if (temp.size()==0) temp.add(mx);
                    else
                    {
                        if (temp.get(temp.size()-1).getBondad(MTCopia,S_V)<=mx.getBondad(MTCopia,S_V)) //Validamos primero con el ultimo elemento
                        {
                            temp.add(mx);
                            continue;
                        }

                        for (int i=0;i<temp.size();i++)
                        {
                          if (temp.get(i).getBondad(MTCopia,S_V)>mx.getBondad(MTCopia,S_V))  
                          {
                            temp.add(i, mx); 
                            break;
                          }
                        }
                    }
                }
            }
        }
        else
        {
           Collections.sort(lm, new Comparator<Medico>() {

               @Override
               public int compare(Medico o1, Medico o2) {
                   return Double.compare(o1.getBondad(MT, S_V), o2.getBondad(MT, S_V));
               }
           });           
           return lm;
        }
        return temp;
    }
   
    public void InicializarMatrices()
    {
        MA=new int[ConfigAlgoritmo.N_PACIENTES][ConfigAlgoritmo.N_MEDICOS];
        MO=new int[ConfigAlgoritmo.N_PACIENTES][ConfigAlgoritmo.N_MEDICOS];
        S_V=new int[ConfigAlgoritmo.N_MEDICOS];
        listaOrdenAt=new int[ConfigAlgoritmo.N_MEDICOS];
        listaPacientesxPosMedicos=new int[ConfigAlgoritmo.N_PACIENTES];
        bondadMedicoxPaciente=new double[ConfigAlgoritmo.N_PACIENTES];
        for (int m=0;m<ConfigAlgoritmo.N_MEDICOS;m++)
        {
//            listaOrdenAt[m]=0;
//            for (int p=0;p<ConfigAlgoritmo.N_PACIENTES;p++)
//            {
//                MA[p][m]=0;
//                MO[p][m]=0;
//            }
            S_V[m]=1;
        }
        
        for (int p=0;p<ConfigAlgoritmo.N_PACIENTES;p++)
            listaPacientesxPosMedicos[p]=-1;
    }
    
    public double EvaluarCosto()
    {
        double costo=0;
        //Recorremos para cada paciente
        for (Paciente p:listaPacientes)
        {
            int indexPM=listaPacientesxPosMedicos[p.getPosMatriz()];
            if (indexPM==-1) continue;
            int indexP=p.getPosMatriz();
            costo+=p.getBondad()*bondadMedicoxPaciente[indexP]/MA[indexP][indexPM];
        }
        
        return (costo);
    }
    
    
    
    public void MostrarResultado()
    {
        int [][] _MA=(int[][])listaSol.get(0);
//        int [][] _MT=(int[][])listaSol.get(1);
        int [][] _MO=(int[][])listaSol.get(1);
        int [] _listaOrdenAt=(int[])listaSol.get(2);
        
        for (int m=0;m<ConfigAlgoritmo.N_MEDICOS;m++)
        {
           Medico mx=listaMedicos.get(m);
           System.out.println("Medico: "+mx.getNombre());
           
           Paciente[] listaPacientesAtendidosOrdenada=new Paciente[_listaOrdenAt[m]];
           
           for (int p=0;p<ConfigAlgoritmo.N_PACIENTES;p++)
           {
               if (_MA[p][m]!=0)
               {
                   Paciente px=listaPacientes.get(p);
                   listaPacientesAtendidosOrdenada[_MO[p][m]-1]=px;
               }
           }
           
           for (int i=0;i<listaPacientesAtendidosOrdenada.length;i++)
           {
               Paciente p=listaPacientesAtendidosOrdenada[i];
               System.out.println("Paciente: "+p.getNombre()
                                +" Tiempo Atencion: " + _MA[listaPacientesAtendidosOrdenada[i].getPosMatriz()][m]
                                +" Bondad:"+p.getBondad());
           }
           
           listaPacientesAtendidosOrdenada=null;
           System.out.println();
        }
        
    }
    
    public void ejecutarAlgoritmo()
    {
        double fObj=9999999;
               
//        try {
//            file = new FileWriter("CalibracionPacientesDecimas.txt");
//        } catch (IOException ex) {
//            Logger.getLogger(Voraz.class.getName()).log(Level.SEVERE, null, ex);
//        }

        System.out.println("Iniciando Voraz");

        InicializarMatrices();
        ejecutarVoraz();
        fObj=EvaluarCosto();
//        listaSol.add(duplicarMatriz(MA, ConfigAlgoritmo.N_PACIENTES, ConfigAlgoritmo.N_MEDICOS));
        System.out.println(MA+" - "+MA.clone());
        listaSol.add(MA.clone());
        MA=null;
//        listaSol.add(duplicarMatriz(MO, ConfigAlgoritmo.N_PACIENTES, ConfigAlgoritmo.N_MEDICOS));
        listaSol.add(MO.clone());
        MO=null;
//        listaSol.add(duplicarLista(listaOrdenAt, listaOrdenAt.length));
        listaSol.add(listaOrdenAt.clone());
        listaOrdenAt=null;

//        MostrarResultado();
        fObjetivo=fObj;
        System.out.println("FOBJ: "+fObjetivo);
    }
    
    public static void main(String[] args) {
            
             try
        {
            File f=new File("C:\\Users\\PC-HP\\Desktop\\Tesis 2\\Calibracion\\PruebaExperimentacionVoraz.xls");
            WritableWorkbook libro = Workbook.createWorkbook(f);
            WritableSheet hoja = libro.createSheet("Hoja 0", 0);
          
            for (int i=0;i<40;i++)
            {
//                FileWriter archivo = null;
                System.out.println("********************************Archivo: "+i);
                try {

                    int j=1;
//                    for (double alfaP=0.20;alfaP<=0.211;alfaP+=0.001)
//                    for (double alfaM=0.25;alfaM<=0.291;alfaM+=0.005)
//                    for (int it=1000;it<=10500;it+=500)
//                    {
                        ConfigAlgoritmo.ALFAMEDICOS=0.260;
                        ConfigAlgoritmo.ALFAPACIENTES=0.203;
                        ConfigAlgoritmo.N_ITERACIONES_CONST=6500;

                        long timeStart = System.currentTimeMillis();
                        Voraz x = new Voraz();
                        VariablesGenericas vg=new VariablesGenericas();
                        LecturaArchivo.leerDatos(vg,null,x,"C:\\Users\\PC-HP\\Desktop\\Tesis 2\\DatosCalibracion\\datos"+i+".txt");
                        x.ejecutarAlgoritmo();

                        long timeEnd = System.currentTimeMillis();
                        long time = timeEnd - timeStart;
                        System.out.println("Iteraciones: "+ConfigAlgoritmo.N_ITERACIONES_CONST+" Funcion Objetivo:"+(x.fObjetivo/1000)+" Tiempo: "+time);
                        Label lObjetivo=new Label(j,i, (x.fObjetivo/1000)+"");
                        Label lTiempo=new Label(j+1,i, time+"");
                        hoja.addCell(lObjetivo);
                        hoja.addCell(lTiempo);
//                        j+=2;
//                    }  
                } finally {
                    
                }
            }
            libro.write();
            libro.close();
        
        } catch (IOException ex) {
            Logger.getLogger(Grasp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WriteException ex) {
            Logger.getLogger(Grasp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

//timeStart = System.currentTimeMillis();
//timeEnd = System.currentTimeMillis();
//long time = timeEnd - timeStart;
//                out.println(it+" "+fObj + "  " + time);