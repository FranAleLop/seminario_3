package taichi.gui;

import taichi.controller.UsuarioController;
import taichi.model.Usuario; // Opcional, solo si quieres pasar el objeto Usuario logueado

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {

    private UsuarioController usuarioController;

    private JTextField txtNombreUsuario;
    private JPasswordField txtContrasena;
    private JButton btnLogin;

    public LoginFrame(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;

        setTitle("Inicio de Sesión - Gestión Taichi");
        setSize(400, 250); // Tamaño más adecuado para un login
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar la ventana en la pantalla
        setResizable(false); // No permitir redimensionar

        initComponents();
        addListeners();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding alrededor del panel
        add(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título del formulario
        JLabel lblTitulo = new JLabel("Bienvenido", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Ocupa dos columnas
        panel.add(lblTitulo, gbc);

        // Nombre de Usuario
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Usuario:"), gbc);

        txtNombreUsuario = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtNombreUsuario, gbc);

        // Contraseña
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Contraseña:"), gbc);

        txtContrasena = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(txtContrasena, gbc);

        // Botón de Login
        btnLogin = new JButton("Iniciar Sesión");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10); // Más espacio arriba del botón
        panel.add(btnLogin, gbc);
    }

    private void addListeners() {
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarSesion();
            }
        });

        // Permitir iniciar sesión al presionar Enter en los campos de texto
        txtNombreUsuario.addActionListener(e -> iniciarSesion());
        txtContrasena.addActionListener(e -> iniciarSesion());
    }

    private void iniciarSesion() {
        String nombreUsuario = txtNombreUsuario.getText();
        String contrasena = new String(txtContrasena.getPassword());

        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese usuario y contraseña.", "Campos Vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Usuario usuarioLogueado = usuarioController.autenticarUsuario(nombreUsuario, contrasena);

            if (usuarioLogueado != null) {
                // Autenticación exitosa
                JOptionPane.showMessageDialog(this, "Inicio de sesión exitoso. ¡Bienvenido, " + usuarioLogueado.getNombreUsuario() + "!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
                // Cerrar la ventana de login
                this.dispose(); 
                
                // Abrir la ventana principal de la aplicación
                // NOTA: MainFrame debe ser inicializada con los controladores necesarios.
                // Aquí se asume que los controladores ya están instanciados en algún lugar (ej. en la clase principal App)
                // Se debe pasar el usuario logueado para que MainFrame pueda ajustar la interfaz según el rol
                MainFrame mainFrame = new MainFrame(usuarioController.getAlumnoController(), 
                                                    usuarioController.getProfesorController(), 
                                                    usuarioController.getClaseController(),
                                                    usuarioController.getPeriodoCuotaController(),
                                                    usuarioController.getPagoController(),
                                                    usuarioController.getDocumentoEstadoController(),
                                                    usuarioController, // Pasamos el propio usuarioController
                                                    usuarioLogueado); // Pasamos el usuario que se acaba de loguear
                mainFrame.setVisible(true);

            } else {
                // Autenticación fallida
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
                txtContrasena.setText(""); // Limpiar campo de contraseña
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al intentar iniciar sesión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}