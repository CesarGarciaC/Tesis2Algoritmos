/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pe.edu.pucp.tesis.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import pe.edu.pucp.tesis.algorithm.Grasp;
import pe.edu.pucp.tesis.algorithm.VariablesGenericas;
import pe.edu.pucp.tesis.algorithm.Voraz;
import pe.edu.pucp.tesis.config.ConfigAlgoritmo;
import pe.edu.pucp.tesis.model.Medico;
import pe.edu.pucp.tesis.model.Paciente;

/**
 *
 * @author PC-HP
 */
public class LecturaArchivo {
    
    public static int GRASP=1;
    public static int VORAZ=0;
    
    
    /*
    MT
    listaOrdenAt
    listaPacientes
    listaMedicos
    */
    public static void leerDatos(VariablesGenericas varGenericas,Grasp algGrasp,Voraz algVoraz,String pathFile)
    {   
        try {
            File archivo=new File(pathFile);
            FileReader fr;
            fr = new FileReader(archivo);
            BufferedReader br=new BufferedReader(fr);
            
            String linea;
            
            //Leemos numero de iteraciones
//            ConfigAlgoritmo.N_ITERACIONES_CONST=Integer.parseInt(br.readLine());
            //Leemos constante para pacientes
//            ConfigAlgoritmo.ALFAPACIENTES=Double.parseDouble(br.readLine());
            //Leemos constante para medicos
//            ConfigAlgoritmo.ALFAMEDICOS=Double.parseDouble(br.readLine());
            //Leemos cantidad de pacientes
            ConfigAlgoritmo.N_PACIENTES=Integer.parseInt(br.readLine());
            //Leemos cantidad de medicos
            ConfigAlgoritmo.N_MEDICOS=Integer.parseInt(br.readLine());
            //Leemos cantidad de especialidades
            ConfigAlgoritmo.N_ESPECIALIDADES=Integer.parseInt(br.readLine());
            
            varGenericas.MT=new int[7][ConfigAlgoritmo.N_MEDICOS];
            varGenericas.listaOrdenAt=new int[ConfigAlgoritmo.N_MEDICOS];
            
            int cont=0;
            
            //Leemos lista de médicos
            
            while (!(linea=br.readLine()).equalsIgnoreCase("***"))
            {
                //Nombre/CMP/Experiencia/Especialidad/Turnos
                String[] c=linea.split("/");
                String[] t=c[4].split(" ");
                Medico m=new Medico();
                m.setNombre(c[0]);
                m.setCmp(c[1]);
                m.setExperiencia(Integer.parseInt(c[2]));
                m.setEspecialidad(Integer.parseInt(c[3]));
                
                //Agregamos los turnos disponibles a la matriz de turnos
                //disponibles por médico
                int turnosTotales=0;
                for (int i=0;i<7;i++)
                { 
                    varGenericas.MT[i][cont]=Integer.parseInt(t[i]);
                    turnosTotales+=Integer.parseInt(t[i]);
                }
//                m.setTurnosTrabajo(turnosTotales);
                m.setTurnosTrabajo(turnosTotales*ConfigAlgoritmo.N_SEMANAS_TRABAJO);
                m.setPosMatriz(cont);
                varGenericas.listaMedicos.add(m);
                varGenericas.numeroTotalCitas+=turnosTotales*ConfigAlgoritmo.N_SEMANAS_TRABAJO;
                
                
                cont++;
            }
            
            cont=0;
            //Leemos lista de pacientes
            while ((linea=br.readLine())!=null)
            {
                //Nombre/Edad/Carga/Sueldo/Esp.Req/CostoEnfDX/CMP/NumConsultas
                String[] c=linea.split("/");
                Paciente p=new Paciente();
                p.setNombre(c[0]);
                p.setEdad(Integer.parseInt(c[1]));
                p.setCargaFamiliar(Integer.parseInt(c[2]));
                p.setIngresoMensual(Double.parseDouble(c[3]));
                p.setEspecialidadReq(Integer.parseInt(c[4]));
                
                double costo=Double.parseDouble(c[5]);
                p.setCostoEnfDx(costo);
                
                if (costo!=1)
                {
                    p.setMedicoTratante(varGenericas.listaMedicos.get(Integer.parseInt(c[6].split("-")[1])));
                }
                
                p.setNumCitasUltimoAnio(Integer.parseInt(c[7]));
                p.setPosMatriz(cont);
                varGenericas.listaPacientes.add(p);
                cont++;
            }
            
            recuperarInformacion(varGenericas,algGrasp, algVoraz);
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LecturaArchivo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LecturaArchivo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void recuperarInformacion(VariablesGenericas varG, Grasp algGrasp, Voraz algVoraz)
    {
        if (algGrasp!=null)
        {
            algGrasp.MT=varG.MT;
            algGrasp.S=varG.S;
            algGrasp.listaPacientes=varG.listaPacientes;
            algGrasp.listaMedicos=varG.listaMedicos;
            algGrasp.listaOrdenAt=varG.listaOrdenAt;
        }
        
        if (algVoraz!=null)
        {
            algVoraz.MT=varG.MT;
            algVoraz.S=varG.S;
            algVoraz.listaPacientes=varG.listaPacientes;
            algVoraz.listaMedicos=varG.listaMedicos;
            algVoraz.listaOrdenAt=varG.listaOrdenAt;
        }
    }
}
