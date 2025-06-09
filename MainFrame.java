package taichi.gui;

import javax.swing.*;
import java.awt.*;

// Importa los controladores que se pasarán a los paneles específicos
import taichi.controller.AlumnoController;
import taichi.controller.ProfesorController;
import taichi.controller.ClaseController;
import taichi.controller.PeriodoCuotaController;
import taichi.controller.PagoController;
import taichi.controller.DocumentoEstadoController;
import taichi.controller.UsuarioController;
import taichi.model.Usuario;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;

    // Instancias de los controladores
    private AlumnoController alumnoController;
    private ProfesorController profesorController;
    private ClaseController claseController;
    private PeriodoCuotaController periodoCuotaController;
    private PagoController pagoController;
    private DocumentoEstadoController documentoEstadoController;
    private UsuarioController usuarioController;

    public MainFrame() {
        setTitle("Sistema de Gestión Taichi Academy");
        setSize(1000, 700); // Tamaño inicial de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cierra la aplicación al cerrar la ventana
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        // Inicializar los controladores
        alumnoController = new AlumnoController();
        profesorController = new ProfesorController();
        claseController = new ClaseController();
        periodoCuotaController = new PeriodoCuotaController();
        pagoController = new PagoController();
        documentoEstadoController = new DocumentoEstadoController();
        usuarioController = new UsuarioController();

        // Inicializar JTabbedPane
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // Crear e añadir los paneles individuales a las pestañas
        // Cada panel recibirá las instancias de los controladores que necesite
        
        // 1. Panel de Gestión de Alumnos
        AlumnoPanel alumnoPanel = new AlumnoPanel(alumnoController); // AlumnoPanel aún no existe
        tabbedPane.addTab("Alumnos", alumnoPanel);

        // 2. Panel de Gestión de Profesores
        ProfesorPanel profesorPanel = new ProfesorPanel(profesorController); // ProfesorPanel aún no existe
        tabbedPane.addTab("Profesores", profesorPanel);

        // 3. Panel de Gestión de Clases
        ClasePanel clasePanel = new ClasePanel(claseController); // ClasePanel aún no existe
        tabbedPane.addTab("Clases", clasePanel);

        // 4. Panel de Gestión de Cuotas y Pagos
        // Este panel podría necesitar varios controladores para su lógica
        PagoPanel pagoPanel = new PagoPanel(pagoController, alumnoController, periodoCuotaController); // PagoPanel aún no existe
        tabbedPane.addTab("Pagos", pagoPanel);

        // 5. Panel de Gestión de Períodos de Cuota
        PeriodoCuotaPanel periodoCuotaPanel = new PeriodoCuotaPanel(periodoCuotaController); // PeriodoCuotaPanel aún no existe
        tabbedPane.addTab("Períodos Cuota", periodoCuotaPanel);

        // 6. Panel de Gestión de Documentos de Alumnos
        DocumentoEstadoPanel documentoEstadoPanel = new DocumentoEstadoPanel(documentoEstadoController, alumnoController); // DocumentoEstadoPanel aún no existe
        tabbedPane.addTab("Documentos Alumnos", documentoEstadoPanel);

        // 7. Panel de Gestión de Usuarios (para administradores)
        UsuarioPanel usuarioPanel = new UsuarioPanel(usuarioController); // UsuarioPanel aún no existe
        tabbedPane.addTab("Usuarios", usuarioPanel);

        // NOTA: Podemos añadir un panel de "Bienvenida" o "Dashboard" si es necesario.
        // DashboardPanel dashboardPanel = new DashboardPanel(alumnoController, pagoController);
        // tabbedPane.addTab("Dashboard", dashboardPanel);
    }

    public MainFrame(String alumnoController2, GraphicsConfiguration profesorController2, Object claseController2,
            Object periodoCuotaController2, Object pagoController2, Object documentoEstadoController2,
            UsuarioController usuarioController2, Usuario usuarioLogueado) {
        
    }

    public static void main(String[] args) {
        // Ejecutar la ventana principal en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}