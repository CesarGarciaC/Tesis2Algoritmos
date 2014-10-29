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
import pe.edu.pucp.tesis.config.ConfigAlgoritmo;
import pe.edu.pucp.tesis.model.Medico;
import pe.edu.pucp.tesis.model.Paciente;
import pe.edu.pucp.tesis.util.LecturaArchivo;
import jxl.*;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 *
 * @author PC-HP
 */
public class Grasp {

    public int[][] MA=null,MT=null,MO=null;
    public int []S=null;
    public List<Paciente> listaPacientes=new ArrayList<>();
    public List<Medico> listaMedicos=new ArrayList<>();
    public List<Object> listaSol=new ArrayList<>();
    public int[] listaPacientesxPosMedicos;
    public int[] listaOrdenAt;
    public double[] bondadMedicoxPaciente;
    public double fObjetivo;
    int pacientesAtendidos;
    
    public void ejecutarFaseConstruccion()
    {
        List<Paciente> PCL=null; //Lista Pacientes
        List<Medico> MCL=null; //Lista Medicos
        List<Paciente> listaPendientesP;
        List<Medico> listaPendientesM;
        int [][]MTCopia=duplicarMT();
        
        for (int e=1;e<=ConfigAlgoritmo.N_ESPECIALIDADES;e++)
        {
            PCL=new ArrayList<>();
            MCL=new ArrayList<>();     
            listaPendientesM=OrdenarBondadMedicos(null,MTCopia,e,1);
            listaPendientesP=OrdenarBondadPacientes(e,listaPendientesM);
            
            while ((!listaPendientesM.isEmpty()) && (!listaPendientesP.isEmpty()))
            {
                //Obtenemos el mejor y peor valor para paciente                
                double minP=listaPendientesP.get(0).getBondad();
                double maxP=listaPendientesP.get(listaPendientesP.size()-1).getBondad();
                
                //Recorremos la lista de pacientes pendientes y agregamos a la PCL
                //aquellos que cumplan con la condicion del algoritmo 
                
//                System.out.println(minP+" - "+maxP);
                for (Paciente p:listaPendientesP)
                {
//                    System.out.println(p.getBondad()+" "+p.isEnListaRCL());
                    if(p.isEnListaRCL()) continue;
                    double bondadP=p.getBondad();
                    if ((bondadP<=maxP) && (bondadP>=maxP-ConfigAlgoritmo.ALFAPACIENTES*(maxP-minP)))
                    {
                        p.setEnListaRCL(true);
                        PCL.add(p);
                    }
                }
                
                //Seleccionamos aleatoriamente un paciente del PCL
                Paciente pElegido=PCL.get(new Random().nextInt(PCL.size()));
                
                if (!todosPendientes(listaPendientesM))
                {                      
                    double minM=listaPendientesM.get(obtenerMinimoMedico(listaPendientesM)).getBondad(MTCopia,S);
                    double maxM=listaPendientesM.get(obtenerMaximoMedico(listaPendientesM)).getBondad(MTCopia,S);

                    //Recorremos la lista de pacientes pendientes y agregamos a la MCL
                    //aquellos que cumplan con la condicion del algoritmo 
                    for (Medico m:listaPendientesM )
                    {
                        if (m.isEnListaRCL()) continue;

                        double bondadM=m.getBondad(MTCopia,S);
                        if ((bondadM<=maxM) && (bondadM>=maxM-ConfigAlgoritmo.ALFAMEDICOS*(maxM-minM)))
                        {
                            m.setEnListaRCL(true);
                            MCL.add(m);
                        }
                    }
                }
                
                //Seleccionamos aleatoriamente un medico del PCL
                Medico mElegido;
                if (pElegido.getMedicoTratante()!=null)
                {                    
                    mElegido=pElegido.getMedicoTratante();
                    if (mElegido.getTurnosDisponibles()<=0)
                    {
                        listaPendientesP.remove(pElegido);
                        PCL.remove(pElegido);
                        continue;
                    }
                }
                else
                    mElegido=MCL.get(new Random().nextInt(MCL.size()));
                
                int tAtencion=7*(S[mElegido.getPosMatriz()]-1) + obtenerDiaSemanaLibre(MTCopia, mElegido);
//                totalBondadPacientesAtendidos+=pElegido.getBondad()*mElegido.getBondad(MTCopia, S);
                bondadMedicoxPaciente[pElegido.getPosMatriz()]=mElegido.getBondad(MTCopia, S);
                
                if (tAtencion==-1)
                {
                    System.out.println("Error tAtencion");
                }
                
                MA[pElegido.getPosMatriz()][mElegido.getPosMatriz()]=tAtencion;
                listaOrdenAt[mElegido.getPosMatriz()]=listaOrdenAt[mElegido.getPosMatriz()]+1;
                MO[pElegido.getPosMatriz()][mElegido.getPosMatriz()]=listaOrdenAt[mElegido.getPosMatriz()];
                mElegido.reducirCitasDisponibles(MTCopia);
//                System.out.println("Reducir: "+mElegido.getNombre()+" D: "+mElegido.getTurnosDisponibles() +" M: "+mElegido);
                listaPacientesxPosMedicos[pElegido.getPosMatriz()]=mElegido.getPosMatriz();
                
//                if (mElegido.getTurnosDisponibles()==0)
                if (mElegido.getTurnosDisponiblesSemana()==0)
                {
                    if (S[mElegido.getPosMatriz()]==ConfigAlgoritmo.N_SEMANAS_TRABAJO)
                        listaPendientesM.remove(mElegido);
                    else
                    {
//                        System.out.println(mElegido.getTurnosDisponibles()+" / "+S[mElegido.getPosMatriz()]);
//                        mElegido.setTurnosDisponibles(mElegido.getTurnosTrabajo());
                        mElegido.setTurnosDisponiblesSemana(mElegido.getTurnosTrabajo()/ConfigAlgoritmo.N_SEMANAS_TRABAJO);
                        S[mElegido.getPosMatriz()]++;
                        ReiniciarMatrizTurnos(MTCopia,mElegido.getPosMatriz());
                    }
                }
                
                mElegido.setEnListaRCL(false);
//                pElegido.setEnListaRCL(false);
                listaPendientesP.remove(pElegido);
                PCL.remove(pElegido);
                MCL.remove(mElegido);
                listaPendientesM=OrdenarBondadMedicos(listaPendientesM,MTCopia,e,0);
            }
        }
        PCL=null;
        MCL=null;
        listaPendientesM=null;
        listaPendientesP=null;
        MTCopia=null;
    }
    
    public void ReiniciarMatrizTurnos(int [][]MTCopia,int indexM)
    {
        for (int i=0;i<7;i++)
            MTCopia[i][indexM]=MT[i][indexM];
    }
    
    public Medico obtenerMedicoListaPendiente(List<Medico> lm, String cmp)
    {
        for (Medico m:lm)
        {
            if (m.getCmp().equalsIgnoreCase(cmp))
                return m;
        }
        return null;
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
        return -1;
    }
    
    public List<Paciente> OrdenarBondadPacientes(int esp,List<Medico> lpm)
    {
        List<Paciente> temp=new ArrayList<>();
        for (Paciente p: listaPacientes)
        {
            if (p.getEspecialidadReq()==esp)
            {
                Paciente px=new Paciente(p);
                
                if (p.getMedicoTratante()!=null)
                {
                    for (Medico m:lpm)
                    {
                        if (px.getMedicoTratante().getCmp().equalsIgnoreCase(m.getCmp()))
                            px.setMedicoTratante(m);
                    }
                }
                
                temp.add(px);
            }
        }        
        
         Collections.sort(temp, new Comparator<Paciente>() {

               @Override
               public int compare(Paciente o1, Paciente o2) {
                   return Double.compare(o1.getBondad(), o2.getBondad());
               }
           });
        
        return temp;
    }
        
    public List<Medico> OrdenarBondadMedicos(List<Medico> lm,final int [][]MTCopia,int esp,int f)
    {
        List<Medico> temp=new ArrayList<>();
        int x=0;
        
        if (f==1)
        {
            for (Medico m: listaMedicos)
            {
                if (m.getEspecialidad()==esp)
                {
                    x++;
                    Medico mx=new Medico(m);

                    if (temp.size()==0) temp.add(mx);
                    else
                    {
                        if (temp.get(temp.size()-1).getBondad(MTCopia,S)<=mx.getBondad(MTCopia,S)) //Validamos primero con el ultimo elemento
                        {
                            temp.add(mx);
                            continue;
                        }

                        for (int i=0;i<temp.size();i++)
                        {
                          if (temp.get(i).getBondad(MTCopia,S)>mx.getBondad(MTCopia,S))  
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
                   return Double.compare(o1.getBondad(MTCopia, S), o2.getBondad(MTCopia, S));
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
        S=new int[ConfigAlgoritmo.N_MEDICOS];
        listaOrdenAt=new int[ConfigAlgoritmo.N_MEDICOS];
        listaPacientesxPosMedicos=new int[ConfigAlgoritmo.N_PACIENTES];
        bondadMedicoxPaciente=new double[ConfigAlgoritmo.N_PACIENTES];
//        totalBondadPacientesAtendidos=0;
        for (int m=0;m<ConfigAlgoritmo.N_MEDICOS;m++)
        {
//            listaOrdenAt[m]=0;
//            for (int p=0;p<ConfigAlgoritmo.N_PACIENTES;p++)
//            {
//                MA[p][m]=0;
//                MO[p][m]=0;
//            }
            S[m]=1;
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
    
    public void ejecutarFaseMejora()
    {
        for (int c=0;c<ConfigAlgoritmo.N_MEDICOS;c++)
        {
            for (int f=0;f<ConfigAlgoritmo.N_PACIENTES-1;f++)
            {
                if (MA[f][c]!=0)
                {
                   for (int fx=0;fx<ConfigAlgoritmo.N_PACIENTES;fx++) 
                   {
                       if (MA[fx][c]!=0)
                       {
                           if (MA[fx][c]==MA[f][c]) continue;
                            
                           Paciente p1=listaPacientes.get(f);
                           Paciente p2=listaPacientes.get(fx);
                           
                           double difOriginal=(p1.getBondad()*bondadMedicoxPaciente[p1.getPosMatriz()]/MA[f][c])+
                                              (p2.getBondad()*bondadMedicoxPaciente[p2.getPosMatriz()]/MA[fx][c]);
                           double difCambio=(p1.getBondad()*bondadMedicoxPaciente[p2.getPosMatriz()]/MA[fx][c])+
                                              (p2.getBondad()*bondadMedicoxPaciente[p1.getPosMatriz()]/MA[f][c]);
                           
                           if (difCambio>difOriginal)
                           {
                               int aTemp=MA[f][c];
                               int oTemp=MO[f][c];
                               double bTemp=bondadMedicoxPaciente[p1.getPosMatriz()];
                               MA[f][c]=MA[fx][c];
                               MA[fx][c]=aTemp;
                               MO[f][c]=MO[fx][c];
                               MO[fx][c]=oTemp;
                               bondadMedicoxPaciente[p1.getPosMatriz()]=bondadMedicoxPaciente[p2.getPosMatriz()];
                               bondadMedicoxPaciente[p2.getPosMatriz()]=bTemp;
                           }
                       }
                   }
                }
            }
        }
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
        FileWriter file=null;
        double fObj=9999999;
        boolean firstIt;
               
        try {
            file = new FileWriter("CalibracionPacientesDecimas.txt");
        } catch (IOException ex) {
            Logger.getLogger(Grasp.class.getName()).log(Level.SEVERE, null, ex);
        }

//        PrintWriter out = new PrintWriter(file);
        System.out.println("Iniciando GRASP");
//        ConfigAlgoritmo.ALFAPACIENTES=0.5;  
//        ConfigAlgoritmo.ALFAMEDICOS=0.5;
        ConfigAlgoritmo.N_ITERACIONES_CONST=1000;
          
                firstIt=true;
                for (int i=1;i<=ConfigAlgoritmo.N_ITERACIONES_CONST;i++)
                {
                     InicializarMatrices();
                     ejecutarFaseConstruccion();
                     ejecutarFaseMejora();
                     double fObjTemp=EvaluarCosto();

                     if (firstIt)
                     {
                         fObj=fObjTemp;
                         firstIt=false;
                         //LIsta solucion MA - MT - MO
                            listaSol.clear();
                            listaSol.add(MA.clone());
                            MA=null;
                            listaSol.add(MO.clone());
                            MO=null;
                            listaSol.add(listaOrdenAt.clone());
                            listaOrdenAt=null;
                     }
                     else
                     {
                        if (fObjTemp>fObj)
                        {
                            //LIsta solucion MA - MT - MO
                            listaSol.clear();
                            listaSol.add(MA.clone());
                            MA=null;
                            listaSol.add(MO.clone());
                            MO=null;
                            listaSol.add(listaOrdenAt.clone());
                            listaOrdenAt=null;
                            fObj=fObjTemp;
                        }
                     }
                }
//        out.println(fObj);
//        MostrarResultado();
        fObjetivo=fObj;
        System.out.println("FOBJ: "+fObjetivo);
//        out.close();        
    }
    
    public static void main(String[] args) {
        try
        {
            File f=new File("C:\\Users\\PC-HP\\Desktop\\Tesis 2\\Calibracion\\CalibracionAlfaPacientes.xls");

            WritableWorkbook libro = Workbook.createWorkbook(f);
            WritableSheet hoja = libro.createSheet("Hoja 0", 0);
          
            for (int i=0;i<40;i++)
            {
//                FileWriter archivo = null;
                System.out.println("********************************Archivo: "+i);
                try {

                    int j=1;
                    for (double alfaP=0.2;alfaP<=0.81;alfaP+=0.05)
                    {
                        ConfigAlgoritmo.ALFAMEDICOS=0.5;
                        ConfigAlgoritmo.ALFAPACIENTES=alfaP;
                        ConfigAlgoritmo.N_ITERACIONES_CONST=1000;

                        long timeStart = System.currentTimeMillis();
                        Grasp x = new Grasp();
                        VariablesGenericas vg=new VariablesGenericas();
                        LecturaArchivo.leerDatos(vg,x,null,"C:\\Users\\PC-HP\\Desktop\\Tesis 2\\DatosCalibracion\\datos"+i+".txt");
                        x.ejecutarAlgoritmo();

                        long timeEnd = System.currentTimeMillis();
                        long time = timeEnd - timeStart;
                        System.out.println("AlfaPaciente: "+alfaP+" Funcion Objetivo:"+(x.fObjetivo/1000)+" Tiempo: "+time);
                        Label lObjetivo=new Label(j,i, (x.fObjetivo/1000)+"");
//                        Label lTiempo=new Label(j+1,i, time+"");
                        hoja.addCell(lObjetivo);
                        j++;
                    }  
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