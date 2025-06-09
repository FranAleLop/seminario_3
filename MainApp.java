package taichi.gui;

import javax.swing.SwingUtilities;
import taichi.gui.MainFrame; 

public class MainApp {

    public static void main(String[] args) {
        // Asegúrate de que la GUI se ejecute en el Event Dispatch Thread (EDT)
        // Esto es una buena práctica en Swing para evitar problemas de concurrencia.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Inicializa la conexión a la base de datos al inicio de la aplicación
                    // Esto es opcional, pero puede ser útil para verificar la conexión temprano.
                    // com.taichi.dao.DatabaseConnection.getConnection().close(); // Para probar la conexión
                    
                    // Crea y muestra la ventana principal
                    MainFrame mainFrame = new MainFrame();
                    mainFrame.setVisible(true);
                    System.out.println("Aplicación Taichi Academy iniciada.");
                } catch (Exception e) {
                    // Loggea cualquier error que impida el inicio de la aplicación
                    e.printStackTrace();
                    System.err.println("Error al iniciar la aplicación: " + e.getMessage());
                    javax.swing.JOptionPane.showMessageDialog(null, 
                        "Error al iniciar la aplicación: " + e.getMessage(), 
                        "Error Crítico", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}