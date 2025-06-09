package taichi.gui;

import taichi.controller.UsuarioController;
import taichi.model.Usuario;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsuarioPanel extends JPanel {

    private UsuarioController usuarioController;

    // Componentes de la UI
    private JTextField txtIdUsuario;
    private JTextField txtNombreUsuario;
    private JPasswordField txtContrasena; // Para la contraseña
    private JCheckBox chkActivo;

    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar; // O dar de baja
    private JButton btnActivar;
    private JButton btnLimpiar;

    private JTable usuarioTable;
    private DefaultTableModel tableModel;

    public UsuarioPanel(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
        setLayout(new BorderLayout());

        // --- Panel de Formulario ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Detalles del Usuario"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Márgenes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Inicializar componentes
        txtIdUsuario = new JTextField(10);
        txtIdUsuario.setEditable(false);
        txtNombreUsuario = new JTextField(20);
        txtContrasena = new JPasswordField(20);
        chkActivo = new JCheckBox("Activo");

        // Añadir etiquetas y campos al formulario
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("ID Usuario:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtIdUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Nombre de Usuario:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtNombreUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtContrasena, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; formPanel.add(chkActivo, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // --- Panel de Botones ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar Nuevo");
        btnActualizar = new JButton("Actualizar");
        btnDesactivar = new JButton("Desactivar Usuario");
        btnActivar = new JButton("Activar Usuario");
        btnLimpiar = new JButton("Limpiar Campos");

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnDesactivar);
        buttonPanel.add(btnActivar);
        buttonPanel.add(btnLimpiar);

        // Añadir paneles de formulario y botones al panel principal
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Configuración de la Tabla ---
        String[] columnNames = {"ID", "Nombre de Usuario", "Rol", "Activo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que las celdas de la tabla no sean editables
            }
        };
        usuarioTable = new JTable(tableModel);
        usuarioTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(usuarioTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Listeners de Eventos ---
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnDesactivar.addActionListener(e -> desactivarUsuario());
        btnActivar.addActionListener(e -> activarUsuario());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        usuarioTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && usuarioTable.getSelectedRow() != -1) {
                mostrarUsuarioSeleccionado();
            }
        });

        // Cargar usuarios al iniciar el panel
        cargarUsuarios();
    }

    private void guardarUsuario() {
        try {
            if (txtNombreUsuario.getText().isEmpty() || txtContrasena.getPassword().length == 0 ) {
                JOptionPane.showMessageDialog(this, "Nombre de Usuario, Contraseña  son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String contrasena = new String(txtContrasena.getPassword());

            Usuario nuevoUsuario = usuarioController.registrarNuevoUsuario(
                    txtNombreUsuario.getText(),
                    contrasena,
                    chkActivo.isSelected()
            );

            JOptionPane.showMessageDialog(this, "Usuario guardado con éxito. ID: " + nuevoUsuario.getIdUsuario(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarUsuarios();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private void desactivarUsuario() {
        if (txtIdUsuario.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla para desactivar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de desactivar a este usuario?", "Confirmar Desactivación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idUsuario = Integer.parseInt(txtIdUsuario.getText());
                boolean exito = usuarioController.desactivarUsuario(idUsuario);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Usuario desactivado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarUsuarios();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo desactivar el usuario.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID de usuario inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al desactivar usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void activarUsuario() {
        if (txtIdUsuario.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla para activar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de activar a este usuario?", "Confirmar Activación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idUsuario = Integer.parseInt(txtIdUsuario.getText());
                boolean exito = usuarioController.activarUsuario(idUsuario);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Usuario activado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarUsuarios();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo activar el usuario.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID de usuario inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al activar usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarCampos() {
        txtIdUsuario.setText("");
        txtNombreUsuario.setText("");
        txtContrasena.setText("");
        chkActivo.setSelected(true); // Nuevo usuario activo por defecto
        usuarioTable.clearSelection();
    }

    private void cargarUsuarios() {
        tableModel.setRowCount(0); // Limpiar la tabla
        try {
            List<Usuario> usuarios = usuarioController.obtenerTodosLosUsuarios();
            for (Usuario usuario : usuarios) {
                tableModel.addRow(new Object[]{
                    usuario.getIdUsuario(),
                    usuario.getNombreUsuario(),
                    usuario.isActivo() ? "Sí" : "No"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void mostrarUsuarioSeleccionado() {
        int selectedRow = usuarioTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtIdUsuario.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombreUsuario.setText(tableModel.getValueAt(selectedRow, 1).toString());
            // No cargamos la contraseña directamente en el campo de texto por seguridad
            txtContrasena.setText(""); 

            chkActivo.setSelected(tableModel.getValueAt(selectedRow, 3).equals("Sí"));
        }
    }
}