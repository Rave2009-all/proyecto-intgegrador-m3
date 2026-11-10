package com.example;

import com.example.model.Categoria;
import com.example.model.DetalleReserva;
import com.example.model.DetalleReservaId;
import com.example.model.Editorial;
import com.example.model.Libro;
import com.example.model.Reservas;
import com.example.model.Usuarios;
import com.example.service.CorreoService;
import com.example.service.CrudService;
import com.example.service.ReporteService;
import com.example.util.CodigoQR;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;

@PageTitle("Sistema de reservas de libros")
@Route("")
public class MainView extends VerticalLayout {

    private final CrudService crudService;
    private final CorreoService correoService;
    private final ReporteService reporteService;
    private final String pinAdmin;
    private final Div menuAdmin = new Div();
    private final Div puertaAdmin = new Div();
    private int intentosFallidos = 0;
    private long bloqueadoHasta = 0;
    private String busquedaPendiente = "";
    private int paginaActual = 0;
    private final Div pagina = new Div();
    private static final int DIAS_PRESTAMO = 14;
    private static final String SIN_FAVORITOS = "Aún no tienes libros favoritos. Abre un libro y pulsa Agregar a Favoritos.";
    private final List<Runnable> aperturas = new ArrayList<>();
    private static final String[] PORTADAS = {
        "linear-gradient(160deg, #7a1020, #16070a)",
        "linear-gradient(160deg, #1c3c6e, #0a1a36)",
        "linear-gradient(160deg, #efe4c9, #d3c195)",
        "linear-gradient(160deg, #2f6b4f, #143826)",
        "linear-gradient(160deg, #8a5a2b, #4a2d12)",
        "linear-gradient(160deg, #f4ead2, #dccaa0)"
    };

    public MainView(
        CrudService crudService,
        CorreoService correoService,
        ReporteService reporteService,
        @Value("${bookly.admin.pin:admin123}") String pinAdmin
    ) {
        this.crudService = crudService;
        this.correoService = correoService;
        this.reporteService = reporteService;
        this.pinAdmin = pinAdmin;
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("app-shell");

        String[] titulos = {"Inicio", "Catálogo", "Mis Reservas", "Usuarios", "Reservas", "Libros",
            "Editoriales", "Categorías", "Detalle Reserva", "Códigos QR", "Correo", "Reportes"};
        VaadinIcon[] iconos = {VaadinIcon.HOME, VaadinIcon.BOOK, VaadinIcon.CALENDAR_CLOCK, VaadinIcon.USERS,
            VaadinIcon.CLIPBOARD_TEXT, VaadinIcon.ARCHIVE, VaadinIcon.BUILDING, VaadinIcon.TAGS, VaadinIcon.LIST,
            VaadinIcon.QRCODE, VaadinIcon.ENVELOPE, VaadinIcon.FILE_TEXT};
        List<Supplier<Component>> vistas = List.of(
            this::crearInicio,
            this::crearCatalogo,
            this::crearMisReservas,
            () -> vistaAdmin("Usuarios", "Personas registradas en la biblioteca", crearSeccionUsuarios()),
            () -> vistaAdmin("Reservas", "Reservas registradas en el sistema", crearSeccionReservas()),
            () -> vistaAdmin("Libros", "Catálogo de libros disponibles", crearSeccionLibros()),
            () -> vistaAdmin("Editoriales", "Casas editoriales de los libros", crearSeccionEditoriales()),
            () -> vistaAdmin("Categorías", "Géneros y temas para clasificar los libros", crearSeccionCategorias()),
            () -> vistaAdmin("Detalle Reserva", "Libros incluidos en cada reserva", crearSeccionDetalleReserva()),
            () -> vistaHerramienta("Códigos QR", "Genera y lee códigos QR", crearSeccionQR()),
            () -> vistaHerramienta("Correo", "Envía correos electrónicos", crearSeccionCorreo()),
            () -> vistaHerramienta("Reportes", "Genera informes en PDF", crearSeccionReportes())
        );

        pagina.addClassName("page-body");
        Div contenido = new Div(pagina);
        contenido.addClassName("main-content");

        Div menu = new Div();
        menu.addClassName("nav");
        menuAdmin.addClassName("nav-admin");
        List<Div> items = new ArrayList<>();
        for (int i = 0; i < titulos.length; i++) {
            int indice = i;
            Div item = crearItemMenu(titulos[i], iconos[i]);
            Runnable abrir = () -> {
                if (indice >= 3 && !adminActivo()) {
                    pedirClave(() -> aperturas.get(indice).run());
                    return;
                }
                paginaActual = indice;
                items.forEach(it -> it.removeClassName("selected"));
                item.addClassName("selected");
                pagina.removeAll();
                pagina.add(vistas.get(indice).get());
                contenido.getElement().executeJs("this.scrollTop = 0");
            };
            item.addClickListener(e -> abrir.run());
            item.getElement().addEventListener("keydown", e -> abrir.run()).setFilter("event.key === 'Enter'");
            items.add(item);
            aperturas.add(abrir);
            if (indice >= 3) {
                menuAdmin.add(item);
            } else {
                menu.add(item);
            }
        }

        Div app = new Div(crearBarraLateral(menu), contenido);
        app.addClassName("app");
        add(app);

        actualizarAdmin();
        aperturas.get(0).run();
    }

    private void irA(int indice) {
        aperturas.get(indice).run();
    }

    // ==========================================
    // 1. usuarios
    // ==========================================
    private Component crearSeccionUsuarios() {
        VerticalLayout layout = new VerticalLayout();
         TextField usuarioIdField = new TextField("ID_Usuario");
        TextField nombreUsuarioField = new TextField("Nombre_Usuario");
        TextField emailUsuarioField = new TextField("Email_Usuario");
        TextField tipoUsuarioField = new TextField("Tipo_Usuario");

        FormLayout form = new FormLayout(usuarioIdField, nombreUsuarioField, emailUsuarioField, tipoUsuarioField);

        Grid<Usuarios> grid = new Grid<>(Usuarios.class, false);
        grid.addColumn(Usuarios::getusuarioId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Usuarios::getnombreUsuario).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Usuarios::getemailUsuario).setHeader("Email").setAutoWidth(true);
        grid.addColumn(Usuarios::gettipoUsuario).setHeader("Tipo").setAutoWidth(true);

        Runnable refrescar = () -> grid.setItems(crudService.listarTodos(Usuarios.class));
        refrescar.run();

        grid.asSingleSelect().addValueChangeListener(e -> {
            Usuarios u = e.getValue();
            if (u != null) {
                usuarioIdField.setValue(u.getusuarioId() != null ? u.getusuarioId().toString() : "");
                nombreUsuarioField.setValue(u.getnombreUsuario() != null ? u.getnombreUsuario() : "");
                emailUsuarioField.setValue(u.getemailUsuario() != null ? u.getemailUsuario() : "");
                tipoUsuarioField.setValue(u.gettipoUsuario() != null ? u.gettipoUsuario() : "");
               
            }
        });

        Button btnCrear = new Button("Crear", e -> {
            Usuarios u = new Usuarios(
                parseId(usuarioIdField),
                nombreUsuarioField.getValue(),
                emailUsuarioField.getValue(),
                tipoUsuarioField.getValue()
            );
            crudService.guardar(u);
            refrescar.run();
            usuarioIdField.setValue(u.getusuarioId() != null ? u.getusuarioId().toString() : "");
            Notification.show("Usuario guardado con ID: " + u.getusuarioId());
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnConsultar = new Button("Consultar", e -> {
            Usuarios u = crudService.buscarPorId(Usuarios.class, parseId(usuarioIdField));
            if (u != null) {
                grid.asSingleSelect().setValue(u);
                Notification.show("Encontrado: " + u.getnombreUsuario());
            } else {
                Notification.show("No encontrado");
            }
        });

        Button btnActualizar = new Button("Actualizar", e -> {
            Usuarios u = crudService.buscarPorId(Usuarios.class, parseId(usuarioIdField));
            if (u != null) {
                u.setusuarioId(parseId(usuarioIdField));
                u.setnombreUsuario(nombreUsuarioField.getValue());
                u.setemailUsuario(emailUsuarioField.getValue());
                u.settipoUsuario(tipoUsuarioField.getValue());

                crudService.actualizar(u);
                refrescar.run();
                Notification.show("Usuario actualizado");
            } else {
                Notification.show("ID no encontrado");
            }
        });

        Button btnEliminar = new Button("Eliminar", e -> {
            crudService.eliminar(Usuarios.class, parseId(usuarioIdField));
            refrescar.run();
            limpiar(usuarioIdField, nombreUsuarioField, emailUsuarioField, tipoUsuarioField);
            Notification.show("Usuario eliminado");
        });
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button btnLimpiar = new Button("Limpiar", e -> {
            limpiar(usuarioIdField, nombreUsuarioField, emailUsuarioField, tipoUsuarioField);
            grid.asSingleSelect().clear();
        });

        layout.add(form, crearAcciones(btnCrear, btnConsultar, btnActualizar, btnEliminar, btnLimpiar), grid);
        return layout;
    }

    // ==========================================
    // 2. Reservas
    // ==========================================
    private Component crearSeccionReservas() {
        VerticalLayout layout = new VerticalLayout();
        TextField idField = new TextField("ID");
        TextField nombreField = new TextField("Nombre");
        TextField descripcionField = new TextField("Descripción");

        FormLayout form = new FormLayout(idField, nombreField, descripcionField);

        Grid<Reservas> grid = new Grid<>(Reservas.class, false);
        grid.addColumn(Reservas::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Reservas::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Reservas::getDescripcion).setHeader("Descripción").setAutoWidth(true);

        Runnable refrescar = () -> grid.setItems(crudService.listarTodos(Reservas.class));
        refrescar.run();

        grid.asSingleSelect().addValueChangeListener(e -> {
            Reservas r = e.getValue();
            if (r != null) {
                idField.setValue(r.getId() != null ? r.getId().toString() : "");
                nombreField.setValue(r.getNombre() != null ? r.getNombre() : "");
                descripcionField.setValue(r.getDescripcion() != null ? r.getDescripcion() : "");
            }
        });

        Button btnCrear = new Button("Crear", e -> {
            Reservas r = new Reservas(nombreField.getValue(), descripcionField.getValue());
            crudService.guardar(r);
            refrescar.run();
            idField.setValue(r.getId() != null ? r.getId().toString() : "");
            Notification.show("Reserva guardada con ID: " + r.getId());
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnConsultar = new Button("Consultar", e -> {
            Reservas r = crudService.buscarPorId(Reservas.class, parseId(idField));
            if (r != null) {
                grid.asSingleSelect().setValue(r);
                Notification.show("Encontrada: " + r.getNombre());
            } else {
                Notification.show("No encontrada");
            }
        });

        Button btnActualizar = new Button("Actualizar", e -> {
            Reservas r = crudService.buscarPorId(Reservas.class, parseId(idField));
            if (r != null) {
                r.setNombre(nombreField.getValue());
                r.setDescripcion(descripcionField.getValue());
                crudService.actualizar(r);
                refrescar.run();
                Notification.show("Reserva actualizada");
            } else {
                Notification.show("ID no encontrado");
            }
        });

        Button btnEliminar = new Button("Eliminar", e -> {
            crudService.eliminar(Reservas.class, parseId(idField));
            refrescar.run();
            limpiar(idField, nombreField, descripcionField);
            Notification.show("Reserva eliminada");
        });
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button btnLimpiar = new Button("Limpiar", e -> {
            limpiar(idField, nombreField, descripcionField);
            grid.asSingleSelect().clear();
        });

        layout.add(form, crearAcciones(btnCrear, btnConsultar, btnActualizar, btnEliminar, btnLimpiar), grid);
        return layout;
    }

    // ==========================================
    // 3. libros
    // ==========================================
    private Component crearSeccionLibros() {
        VerticalLayout layout = new VerticalLayout();
        TextField idField = new TextField("ID");
        TextField tituloField = new TextField("Título");
        TextField autorField = new TextField("Autor");
        TextField portadaField = new TextField("URL de la portada");
        ComboBox<Editorial> editorialBox = new ComboBox<>("Editorial");
        ComboBox<Categoria> categoriaBox = new ComboBox<>("Categoría");

        editorialBox.setItems(crudService.listarTodos(Editorial.class));
        editorialBox.setItemLabelGenerator(Editorial::getNombreEditorial);
        categoriaBox.setItems(crudService.listarTodos(Categoria.class));
        categoriaBox.setItemLabelGenerator(Categoria::getNombreCategoria);

        FormLayout form = new FormLayout(idField, tituloField, autorField, editorialBox, categoriaBox, portadaField);

        Grid<Libro> grid = new Grid<>(Libro.class, false);
        grid.addColumn(Libro::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Libro::getTitulo).setHeader("Título").setAutoWidth(true);
        grid.addColumn(Libro::getAutor).setHeader("Autor").setAutoWidth(true);
        grid.addColumn(l -> l.getEditorial() != null ? l.getEditorial().getNombreEditorial() : "").setHeader("Editorial").setAutoWidth(true);
        grid.addColumn(l -> l.getCategoria() != null ? l.getCategoria().getNombreCategoria() : "").setHeader("Categoría").setAutoWidth(true);

        Runnable refrescar = () -> grid.setItems(crudService.listarTodos(Libro.class));
        refrescar.run();

        grid.asSingleSelect().addValueChangeListener(e -> {
            Libro l = e.getValue();
            if (l != null) {
                idField.setValue(l.getId() != null ? l.getId().toString() : "");
                tituloField.setValue(l.getTitulo() != null ? l.getTitulo() : "");
                autorField.setValue(l.getAutor() != null ? l.getAutor() : "");
                portadaField.setValue(l.getPortada() != null ? l.getPortada() : "");
                editorialBox.setValue(l.getEditorial());
                categoriaBox.setValue(l.getCategoria());
            }
        });

        Button btnCrear = new Button("Crear", e -> {
            Libro l = new Libro(
                tituloField.getValue(),
                autorField.getValue(),
                editorialBox.getValue(),
                categoriaBox.getValue()
            );
            l.setPortada(portadaField.getValue());
            crudService.guardar(l);
            refrescar.run();
            idField.setValue(l.getId() != null ? l.getId().toString() : "");
            Notification.show("Libro guardado con ID: " + l.getId());
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnConsultar = new Button("Consultar", e -> {
            Libro l = crudService.buscarPorId(Libro.class, parseId(idField));
            if (l != null) {
                grid.asSingleSelect().setValue(l);
                Notification.show("Encontrado: " + l.getTitulo());
            } else {
                Notification.show("No encontrado");
            }
        });

        Button btnActualizar = new Button("Actualizar", e -> {
            Libro l = crudService.buscarPorId(Libro.class, parseId(idField));
            if (l != null) {
                l.setTitulo(tituloField.getValue());
                l.setAutor(autorField.getValue());
                l.setPortada(portadaField.getValue());
                l.setEditorial(editorialBox.getValue());
                l.setCategoria(categoriaBox.getValue());
                crudService.actualizar(l);
                refrescar.run();
                Notification.show("Libro actualizado");
            } else {
                Notification.show("ID no encontrado");
            }
        });

        Button btnEliminar = new Button("Eliminar", e -> {
            crudService.eliminar(Libro.class, parseId(idField));
            refrescar.run();
            limpiar(idField, tituloField, autorField, portadaField);
            editorialBox.clear();
            categoriaBox.clear();
            Notification.show("Libro eliminado");
        });
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button btnLimpiar = new Button("Limpiar", e -> {
            limpiar(idField, tituloField, autorField, portadaField);
            editorialBox.clear();
            categoriaBox.clear();
            grid.asSingleSelect().clear();
        });

        layout.add(form, crearAcciones(btnCrear, btnConsultar, btnActualizar, btnEliminar, btnLimpiar), grid);
        return layout;
    }

    // ==========================================
    // 4. editoriales
    // ==========================================
    private Component crearSeccionEditoriales() {
        VerticalLayout layout = new VerticalLayout();
        TextField idField = new TextField("ID");
        TextField nombreField = new TextField("Nombre Editorial");

        FormLayout form = new FormLayout(idField, nombreField);

        Grid<Editorial> grid = new Grid<>(Editorial.class, false);
        grid.addColumn(Editorial::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Editorial::getNombreEditorial).setHeader("Nombre Editorial").setAutoWidth(true);

        Runnable refrescar = () -> grid.setItems(crudService.listarTodos(Editorial.class));
        refrescar.run();

        grid.asSingleSelect().addValueChangeListener(e -> {
            Editorial ed = e.getValue();
            if (ed != null) {
                idField.setValue(ed.getId() != null ? ed.getId().toString() : "");
                nombreField.setValue(ed.getNombreEditorial() != null ? ed.getNombreEditorial() : "");
            }
        });

        Button btnCrear = new Button("Crear", e -> {
            Editorial ed = new Editorial(nombreField.getValue());
            crudService.guardar(ed);
            refrescar.run();
            idField.setValue(ed.getId() != null ? ed.getId().toString() : "");
            Notification.show("Editorial guardada con ID: " + ed.getId());
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnConsultar = new Button("Consultar", e -> {
            Editorial ed = crudService.buscarPorId(Editorial.class, parseId(idField));
            if (ed != null) {
                grid.asSingleSelect().setValue(ed);
                Notification.show("Encontrada: " + ed.getNombreEditorial());
            } else {
                Notification.show("No encontrada");
            }
        });

        Button btnActualizar = new Button("Actualizar", e -> {
            Editorial ed = crudService.buscarPorId(Editorial.class, parseId(idField));
            if (ed != null) {
                ed.setNombreEditorial(nombreField.getValue());
                crudService.actualizar(ed);
                refrescar.run();
                Notification.show("Editorial actualizada");
            } else {
                Notification.show("ID no encontrado");
            }
        });

        Button btnEliminar = new Button("Eliminar", e -> {
            crudService.eliminar(Editorial.class, parseId(idField));
            refrescar.run();
            limpiar(idField, nombreField);
            Notification.show("Editorial eliminada");
        });
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button btnLimpiar = new Button("Limpiar", e -> {
            limpiar(idField, nombreField);
            grid.asSingleSelect().clear();
        });

        layout.add(form, crearAcciones(btnCrear, btnConsultar, btnActualizar, btnEliminar, btnLimpiar), grid);
        return layout;
    }

    // ==========================================
    // 5. categorías
    // ==========================================
    private Component crearSeccionCategorias() {
        VerticalLayout layout = new VerticalLayout();
        TextField idField = new TextField("ID");
        TextField nombreField = new TextField("Nombre Categoría");

        FormLayout form = new FormLayout(idField, nombreField);

        Grid<Categoria> grid = new Grid<>(Categoria.class, false);
        grid.addColumn(Categoria::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Categoria::getNombreCategoria).setHeader("Nombre Categoría").setAutoWidth(true);

        Runnable refrescar = () -> grid.setItems(crudService.listarTodos(Categoria.class));
        refrescar.run();

        grid.asSingleSelect().addValueChangeListener(e -> {
            Categoria c = e.getValue();
            if (c != null) {
                idField.setValue(c.getId() != null ? c.getId().toString() : "");
                nombreField.setValue(c.getNombreCategoria() != null ? c.getNombreCategoria() : "");
            }
        });

        Button btnCrear = new Button("Crear", e -> {
            Categoria c = new Categoria(nombreField.getValue());
            crudService.guardar(c);
            refrescar.run();
            idField.setValue(c.getId() != null ? c.getId().toString() : "");
            Notification.show("Categoría guardada con ID: " + c.getId());
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnConsultar = new Button("Consultar", e -> {
            Categoria c = crudService.buscarPorId(Categoria.class, parseId(idField));
            if (c != null) {
                grid.asSingleSelect().setValue(c);
                Notification.show("Encontrada: " + c.getNombreCategoria());
            } else {
                Notification.show("No encontrada");
            }
        });

        Button btnActualizar = new Button("Actualizar", e -> {
            Categoria c = crudService.buscarPorId(Categoria.class, parseId(idField));
            if (c != null) {
                c.setNombreCategoria(nombreField.getValue());
                crudService.actualizar(c);
                refrescar.run();
                Notification.show("Categoría actualizada");
            } else {
                Notification.show("ID no encontrado");
            }
        });

        Button btnEliminar = new Button("Eliminar", e -> {
            crudService.eliminar(Categoria.class, parseId(idField));
            refrescar.run();
            limpiar(idField, nombreField);
            Notification.show("Categoría eliminada");
        });
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button btnLimpiar = new Button("Limpiar", e -> {
            limpiar(idField, nombreField);
            grid.asSingleSelect().clear();
        });

        layout.add(form, crearAcciones(btnCrear, btnConsultar, btnActualizar, btnEliminar, btnLimpiar), grid);
        return layout;

    }

    // ==========================================
    // 6. detalle de reserva
    // ==========================================
    private Component crearSeccionDetalleReserva() {
        VerticalLayout layout = new VerticalLayout();
        TextField idField = new TextField("Reserva ID");
        ComboBox<Libro> libroBox = new ComboBox<>("Libro");
        IntegerField cantidadField = new IntegerField("Cantidad");
        TextField notasField = new TextField("Notas");

        libroBox.setItems(crudService.listarTodos(Libro.class));
        libroBox.setItemLabelGenerator(Libro::getTitulo);

        FormLayout form = new FormLayout(idField, libroBox, cantidadField, notasField);

        Grid<DetalleReserva> grid = new Grid<>(DetalleReserva.class, false);
        grid.addColumn(DetalleReserva::getReservaId).setHeader("Reserva ID").setAutoWidth(true);
        grid.addColumn(d -> d.getLibro() != null ? d.getLibro().getTitulo() : "").setHeader("Libro").setAutoWidth(true);
        grid.addColumn(DetalleReserva::getCantidad).setHeader("Cantidad").setAutoWidth(true);
        grid.addColumn(DetalleReserva::getNotas).setHeader("Notas").setAutoWidth(true);

        Runnable refrescar = () -> grid.setItems(crudService.listarTodos(DetalleReserva.class));
        refrescar.run();

        grid.asSingleSelect().addValueChangeListener(e -> {
            DetalleReserva d = e.getValue();
            if (d != null) {
                idField.setValue(d.getReservaId() != null ? d.getReservaId().toString() : "");
                libroBox.setValue(d.getLibro());
                cantidadField.setValue(d.getCantidad());
                notasField.setValue(d.getNotas() != null ? d.getNotas() : "");
            }
        });

        Button btnCrear = new Button("Crear", e -> {
            DetalleReserva d = new DetalleReserva(
                parseId(idField),
                libroBox.getValue(),
                cantidadField.getValue(),
                notasField.getValue()
            );
            crudService.guardar(d);
            refrescar.run();
            Notification.show("Detalle de reserva guardado");
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnConsultar = new Button("Consultar", e -> {
            if (libroBox.getValue() == null) {
                Notification.show("Selecciona un libro");
                return;
            }
            DetalleReserva d = crudService.buscarPorId(DetalleReserva.class,
                new DetalleReservaId(parseId(idField), libroBox.getValue().getId()));
            if (d != null) {
                grid.asSingleSelect().setValue(d);
                Notification.show("Detalle encontrado");
            } else {
                Notification.show("No encontrado");
            }
        });

        Button btnActualizar = new Button("Actualizar", e -> {
            if (libroBox.getValue() == null) {
                Notification.show("Selecciona un libro");
                return;
            }
            DetalleReserva d = crudService.buscarPorId(DetalleReserva.class,
                new DetalleReservaId(parseId(idField), libroBox.getValue().getId()));
            if (d != null) {
                d.setCantidad(cantidadField.getValue());
                d.setNotas(notasField.getValue());
                crudService.actualizar(d);
                refrescar.run();
                Notification.show("Detalle actualizado");
            } else {
                Notification.show("Detalle no encontrado");
            }
        });

        Button btnEliminar = new Button("Eliminar", e -> {
            if (libroBox.getValue() == null) {
                Notification.show("Selecciona un libro");
                return;
            }
            crudService.eliminar(DetalleReserva.class,
                new DetalleReservaId(parseId(idField), libroBox.getValue().getId()));
            refrescar.run();
            limpiar(idField, notasField);
            libroBox.clear();
            cantidadField.clear();
            Notification.show("Detalle eliminado");
        });
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button btnLimpiar = new Button("Limpiar", e -> {
            limpiar(idField, notasField);
            libroBox.clear();
            cantidadField.clear();
            grid.asSingleSelect().clear();
        });

        layout.add(form, crearAcciones(btnCrear, btnConsultar, btnActualizar, btnEliminar, btnLimpiar), grid);
        return layout;
    }

    // ==========================================
    // 7. ZXING: GENERAR Y LEER CÓDIGOS QR
    // ==========================================
    private Component crearSeccionQR() {
        VerticalLayout layout = new VerticalLayout();

        H2 titulo = new H2("Generador de códigos QR");

        TextField textoQR = new TextField("Texto para el código QR");
        textoQR.setWidthFull();

        VerticalLayout resultado = new VerticalLayout();

        Button generar = new Button("Generar código QR", e -> {
            try {
                if (textoQR.isEmpty()) {
                    Notification.show("Escribe un texto primero");
                    return;
                }

                byte[] imagen = CodigoQR.generarQR(textoQR.getValue());

                StreamResource recurso = new StreamResource(
                    "codigoQR.png",
                    () -> new ByteArrayInputStream(imagen)
                );

                Image qr = new Image(recurso, "Código QR generado");
                qr.setWidth("250px");

                Anchor descargar = new Anchor(recurso, "Descargar código QR");
                descargar.getElement().setAttribute("download", true);

                resultado.removeAll();
                resultado.add(qr, descargar);

                Notification.show("Código QR generado correctamente");
            } catch (Exception ex) {
                Notification.show("Error al generar el código QR: " + ex.getMessage());
            }
        });
        generar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        H2 tituloLectura = new H2("Leer código QR");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload subirImagen = new Upload(buffer);
        subirImagen.setAcceptedFileTypes("image/png", "image/jpeg", ".jpg", ".jpeg");
        subirImagen.setMaxFiles(1);

        subirImagen.addSucceededListener(event -> {
            try {
                String texto = CodigoQR.leerQR(buffer.getInputStream());
                Notification.show("Contenido del QR: " + texto, 5000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("No se pudo leer el código QR: " + ex.getMessage());
            }
        });

        layout.add(titulo, textoQR, generar, resultado, tituloLectura, subirImagen);
        return layout;
    }

    // ==========================================
    // 8. JAKARTA MAIL: ENVÍO DE CORREOS
    // ==========================================
    private Component crearSeccionCorreo() {
        VerticalLayout layout = new VerticalLayout();

        H2 titulo = new H2("Enviar correo electrónico");

        TextField destinatario = new TextField("Correo del destinatario");
        destinatario.setWidthFull();

        TextField asunto = new TextField("Asunto");
        asunto.setWidthFull();

        TextArea mensaje = new TextArea("Mensaje");
        mensaje.setWidthFull();
        mensaje.setHeight("150px");

        Button enviar = new Button("Enviar correo", e -> {
            try {
                if (destinatario.isEmpty() || asunto.isEmpty() || mensaje.isEmpty()) {
                    Notification.show("Completa todos los campos");
                    return;
                }

                correoService.enviarCorreo(
                    destinatario.getValue(),
                    asunto.getValue(),
                    mensaje.getValue()
                );

                Notification.show("Correo enviado correctamente");

                destinatario.clear();
                asunto.clear();
                mensaje.clear();
            } catch (Exception ex) {
                Notification.show("Error al enviar el correo: " + ex.getMessage());
            }
        });
        enviar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(titulo, destinatario, asunto, mensaje, enviar);
        return layout;
    }

    // ==========================================
    // 9. JASPERREPORTS: GENERAR INFORMES PDF
    // ==========================================
    private Component crearSeccionReportes() {
        VerticalLayout layout = new VerticalLayout();

        H2 titulo = new H2("Informes de Bookly");
        H3 descripcion = new H3("Genera un informe con los libros registrados");

        Button generar = new Button("Generar informe de libros");
        VerticalLayout resultado = new VerticalLayout();

        generar.addClickListener(e -> {
            try {
                var libros = crudService.listarTodos(Libro.class);

                byte[] pdf = reporteService.generarReporteLibros(libros);

                StreamResource archivo = new StreamResource(
                    "reporte-libros.pdf",
                    () -> new ByteArrayInputStream(pdf)
                );

                Anchor descargar = new Anchor(archivo, "Descargar informe PDF");
                descargar.getElement().setAttribute("download", true);

                resultado.removeAll();
                resultado.add(descargar);

                Notification.show("Informe generado correctamente");
            } catch (Exception ex) {
                Notification.show("Error al generar el informe: " + ex.getMessage());
            }
        });
        generar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(titulo, descripcion, generar, resultado);
        return layout;
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================
    private Long parseId(TextField field) {
        try {
            return Long.parseLong(field.getValue().trim());
        } catch (Exception e) {
            return null;
        }
    }

   

    private void limpiar(TextField... campos) {
        for (TextField c : campos) {
            c.clear();
        }
    }

    private HorizontalLayout crearAcciones(Button... botones) {
        HorizontalLayout layout = new HorizontalLayout(botones);
        layout.getStyle().set("flex-wrap", "wrap");
        return layout;
    }

    // ==========================================
    // ESTRUCTURA VISUAL
    // ==========================================
    private Component vistaAdmin(String titulo, String descripcion, Component contenido) {
        Div etiqueta = new Div(VaadinIcon.KEY.create(), new Span("Modo administrador"));
        etiqueta.addClassName("admin-tag");
        Div vista = new Div(crearEncabezadoPagina(titulo, descripcion, etiqueta), seccion(contenido));
        vista.addClassName("view");
        return vista;
    }

    private Component vistaHerramienta(String titulo, String descripcion, Component contenido) {
        Div etiqueta = new Div(VaadinIcon.KEY.create(), new Span("Modo administrador"));
        etiqueta.addClassName("admin-tag");
        Div panel = new Div(contenido);
        panel.addClassNames("panel", "panel-tool");
        Div vista = new Div(crearEncabezadoPagina(titulo, descripcion, etiqueta), panel);
        vista.addClassName("view");
        return vista;
    }

    private Component seccion(Component contenido) {
        List<Component> partes = contenido.getChildren().toList();
        Component formulario = partes.get(0);
        Component acciones = partes.get(1);
        Component tabla = partes.get(2);

        if (formulario instanceof FormLayout f) {
            f.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        }
        if (acciones instanceof HasStyle h) {
            h.addClassName("acciones");
        }

        Div panelFormulario = new Div(crearTituloPanel("Formulario", VaadinIcon.EDIT), formulario, acciones);
        panelFormulario.addClassNames("panel", "panel-form");
        Div panelTabla = new Div(crearTituloPanel("Registros", VaadinIcon.TABLE), tabla);
        panelTabla.addClassNames("panel", "panel-table");

        Div area = new Div(panelFormulario, panelTabla);
        area.addClassName("workspace");
        return area;
    }

    private Div crearTituloPanel(String texto, VaadinIcon icono) {
        Div titulo = new Div(icono.create(), new Span(texto));
        titulo.addClassName("panel-title");
        return titulo;
    }

    private Div crearBarraLateral(Div menu) {
        Icon logo = VaadinIcon.BOOK.create();
        logo.addClassName("brand-logo");
        Span nombre = new Span("Bookly");
        nombre.addClassName("brand-name");
        Div marca = new Div(logo, nombre);
        marca.addClassName("brand");

        Div barra = new Div(marca, menu, puertaAdmin, menuAdmin);
        barra.addClassName("sidebar");
        return barra;
    }

    private Div crearItemMenu(String titulo, VaadinIcon icono) {
        Icon ic = icono.create();
        ic.addClassName("nav-icon");
        Div item = new Div(ic, new Span(titulo));
        item.addClassName("nav-item");
        item.getElement().setAttribute("role", "button");
        item.getElement().setAttribute("tabindex", "0");
        return item;
    }

    private Div crearEncabezadoPagina(String titulo, String descripcion, Component accion) {
        H2 t = new H2(titulo);
        t.addClassName("page-title");
        Span d = new Span(descripcion);
        d.addClassName("page-description");
        Div cabecera = new Div(new Div(t, d));
        if (accion != null) {
            cabecera.add(accion);
        }
        cabecera.addClassName("page-header");
        return cabecera;
    }

    private Div crearBarraSuperior() {
        TextField buscador = new TextField();
        buscador.setPlaceholder("Buscar libros, autores o géneros...");
        buscador.setPrefixComponent(VaadinIcon.SEARCH.create());
        buscador.setClearButtonVisible(true);
        buscador.addClassName("hero-search");
        buscador.addKeyDownListener(Key.ENTER, e -> {
            busquedaPendiente = buscador.getValue().trim();
            irA(1);
        });

        Div avatar = new Div(VaadinIcon.USER.create());
        avatar.addClassName("avatar");
        if (adminActivo()) {
            avatar.addClassName("admin");
        }
        avatar.getElement().setAttribute("role", "button");
        avatar.getElement().setAttribute("tabindex", "0");
        avatar.getElement().setAttribute("aria-label", "Mi cuenta");

        ContextMenu menu = new ContextMenu(avatar);
        menu.setOpenOnClick(true);
        MenuItem ingresar = menu.addItem("Ingresar como administrador", e -> pedirClave(() -> irA(3)));
        MenuItem salir = menu.addItem("Cerrar sesión de administrador", e -> {
            VaadinSession.getCurrent().setAttribute("bookly.admin", null);
            actualizarAdmin();
            irA(0);
        });
        ingresar.setVisible(!adminActivo());
        salir.setVisible(adminActivo());
        menu.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                ingresar.setVisible(!adminActivo());
                salir.setVisible(adminActivo());
            }
        });

        Div barra = new Div(buscador, avatar);
        barra.addClassName("hero-topbar");
        return barra;
    }

    private Component crearInicio() {
        Span linea1 = new Span("Lee. Aprende.");
        linea1.addClassName("hero-line");
        Span linea2 = new Span("Descubre.");
        linea2.addClassNames("hero-line", "hero-accent");
        Span texto = new Span("Tu biblioteca, un mundo de conocimiento al alcance de un clic.");
        texto.addClassName("hero-text");
        Button explorar = new Button("Explorar Catálogo", VaadinIcon.ARROW_RIGHT.create(), e -> irA(1));
        explorar.setIconAfterText(true);
        explorar.addClassName("btn-hero");
        explorar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Div ilustracion = new Div();
        ilustracion.addClassName("hero-art");
        Div hero = new Div(new Div(linea1, linea2, texto, explorar), ilustracion, crearBarraSuperior());
        hero.addClassName("hero");

        H3 destacados = new H3("Libros Destacados");
        destacados.addClassName("section-heading");
        Button verTodos = new Button("Ver todos", VaadinIcon.ARROW_RIGHT.create(), e -> irA(1));
        verTodos.setIconAfterText(true);
        verTodos.addClassName("link-all");
        verTodos.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Div tituloSeccion = new Div(new Div(VaadinIcon.STAR.create(), destacados), verTodos);
        tituloSeccion.addClassName("section-title");

        Div tarjetas = new Div();
        tarjetas.addClassName("book-grid");
        List<Libro> libros = crudService.listarTodos(Libro.class);
        if (libros.isEmpty()) {
            tarjetas.add(crearVacio("Aún no hay libros registrados. Agrégalos desde Administración."));
        }
        libros.stream().limit(4).forEach(l -> tarjetas.add(crearTarjetaLibro(l)));

        Div banner = new Div(VaadinIcon.LIGHTBULB.create(), new Span("Una buena lectura siempre es una gran idea."));
        banner.addClassName("banner");

        Div inicio = new Div(hero, tituloSeccion, tarjetas, banner);
        inicio.addClassName("view");
        return inicio;
    }

    private Component crearCatalogo() {
        List<Libro> libros = crudService.listarTodos(Libro.class);
        List<Categoria> categorias = crudService.listarTodos(Categoria.class);

        TextField buscador = new TextField();
        buscador.setPlaceholder("Buscar libros...");
        buscador.setPrefixComponent(VaadinIcon.SEARCH.create());
        buscador.setValueChangeMode(ValueChangeMode.EAGER);
        buscador.setClearButtonVisible(true);
        buscador.addClassName("search");

        Div grid = new Div();
        grid.addClassName("book-grid");
        Div chips = new Div();
        chips.addClassName("chips");
        Categoria[] elegida = {null};

        Runnable pintar = () -> {
            grid.removeAll();
            String q = buscador.getValue().trim().toLowerCase();
            List<Libro> filtrados = libros.stream()
                .filter(l -> elegida[0] == null
                    || (l.getCategoria() != null && elegida[0].getId().equals(l.getCategoria().getId())))
                .filter(l -> q.isEmpty() || contiene(l.getTitulo(), q) || contiene(l.getAutor(), q))
                .toList();
            if (filtrados.isEmpty()) {
                grid.add(crearVacio("No se encontraron libros."));
            }
            filtrados.forEach(l -> grid.add(crearTarjetaLibro(l)));
        };
        buscador.setValue(busquedaPendiente);
        busquedaPendiente = "";
        buscador.addValueChangeListener(e -> pintar.run());

        List<Categoria> opciones = new ArrayList<>();
        opciones.add(null);
        opciones.addAll(categorias);
        List<Div> botones = new ArrayList<>();
        for (Categoria c : opciones) {
            Div chip = new Div(new Span(c == null ? "Todos" : c.getNombreCategoria()));
            chip.addClassName("chip");
            if (c == null) {
                chip.addClassName("selected");
            }
            chip.addClickListener(e -> {
                elegida[0] = c;
                botones.forEach(b -> b.removeClassName("selected"));
                chip.addClassName("selected");
                pintar.run();
            });
            botones.add(chip);
            chips.add(chip);
        }
        pintar.run();

        Div vista = new Div(crearEncabezadoPagina("Catálogo de Libros", "Encuentra tu próxima lectura", buscador), chips, grid);
        vista.addClassName("view");
        return vista;
    }

    private Component crearMisReservas() {
        Button nueva = new Button("Nueva Reserva", VaadinIcon.PLUS.create(), e -> irA(1));
        nueva.addClassName("btn-hero");
        nueva.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Div lista = new Div();
        lista.addClassName("reserva-list");
        List<DetalleReserva> detalles = crudService.listarTodos(DetalleReserva.class);
        if (detalles.isEmpty()) {
            lista.add(crearVacio("Todavía no tienes libros reservados. Explora el catálogo y reserva el primero."));
        }
        detalles.forEach(d -> lista.add(crearFilaReserva(d, lista)));

        Div pestanaReservas = new Div(new Span("Reservas"));
        pestanaReservas.addClassNames("chip", "selected");
        Span textoFavoritos = new Span("Favoritos (" + favoritos().size() + ")");
        Div pestanaFavoritos = new Div(textoFavoritos);
        pestanaFavoritos.addClassName("chip");
        Div pestanas = new Div(pestanaReservas, pestanaFavoritos);
        pestanas.addClassName("chips");

        Div contenido = new Div(lista);
        pestanaReservas.addClickListener(e -> {
            contenido.removeAll();
            contenido.add(lista);
            pestanaReservas.addClassName("selected");
            pestanaFavoritos.removeClassName("selected");
        });
        pestanaFavoritos.addClickListener(e -> {
            contenido.removeAll();
            contenido.add(crearListaFavoritos(textoFavoritos));
            pestanaFavoritos.addClassName("selected");
            pestanaReservas.removeClassName("selected");
        });

        Div vista = new Div(
            crearEncabezadoPagina("Mis Reservas", "Gestiona tus libros reservados y favoritos", nueva),
            pestanas,
            contenido
        );
        vista.addClassName("view");
        return vista;
    }

    private Component crearListaFavoritos(Span contador) {
        Div grid = new Div();
        grid.addClassName("book-grid");
        Set<Long> favs = favoritos();
        List<Libro> libros = new ArrayList<>();
        for (Long id : new ArrayList<>(favs)) {
            Libro libro = crudService.buscarPorId(Libro.class, id);
            if (libro != null) {
                libros.add(libro);
            } else {
                favs.remove(id);
            }
        }
        contador.setText("Favoritos (" + favs.size() + ")");
        if (libros.isEmpty()) {
            grid.add(crearVacio(SIN_FAVORITOS));
        }
        for (Libro libro : libros) {
            Div tarjeta = crearTarjetaLibro(libro);
            Button quitar = new Button("Quitar de favoritos", VaadinIcon.HEART.create(), e -> {
                favs.remove(libro.getId());
                grid.remove(tarjeta);
                contador.setText("Favoritos (" + favs.size() + ")");
                if (favs.isEmpty()) {
                    grid.add(crearVacio(SIN_FAVORITOS));
                }
                Notification.show("Quitado de favoritos");
            });
            quitar.addClassName("btn-quitar");
            quitar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            tarjeta.add(quitar);
            grid.add(tarjeta);
        }
        return grid;
    }

    private Component crearFilaReserva(DetalleReserva d, Div lista) {
        Libro libro = d.getLibro();
        String titulo = libro != null && libro.getTitulo() != null ? libro.getTitulo() : "Libro";
        String autor = libro != null && libro.getAutor() != null ? libro.getAutor() : "";
        Long libroId = libro != null ? libro.getId() : null;

        Div portada = crearPortada(titulo, libro != null ? libro.getPortada() : null);
        portada.addClassName("mini");

        Span nombre = new Span(titulo);
        nombre.addClassName("reserva-title");
        Span detalle = new Span(autor);
        detalle.addClassName("book-author");
        Div info = new Div(nombre, detalle);

        Div datoReserva = crearDato(VaadinIcon.CALENDAR, "Reserva", "N° " + d.getReservaId());
        Div datoCantidad = crearDato(VaadinIcon.LIST, "Cantidad", String.valueOf(d.getCantidad()));

        Span estado = new Span("Reservado");
        estado.addClassName("estado");

        Div fila = new Div();
        fila.addClassName("reserva");
        Button cancelar = new Button(VaadinIcon.TRASH.create(), e -> {
            crudService.eliminar(DetalleReserva.class, new DetalleReservaId(d.getReservaId(), libroId));
            lista.remove(fila);
            Notification.show("Reserva cancelada");
        });
        cancelar.addClassName("btn-icon");
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelar.getElement().setAttribute("aria-label", "Cancelar reserva");

        fila.add(portada, info, datoReserva, datoCantidad, estado, cancelar);
        return fila;
    }

    private Div crearDato(VaadinIcon icono, String etiqueta, String valor) {
        Span e = new Span(etiqueta);
        e.addClassName("dato-label");
        Span v = new Span(valor);
        v.addClassName("dato-valor");
        Div dato = new Div(icono.create(), new Div(e, v));
        dato.addClassName("dato");
        return dato;
    }

    private Div crearTarjetaLibro(Libro libro) {
        String titulo = libro.getTitulo() != null ? libro.getTitulo() : "Sin título";
        Div portada = crearPortada(titulo, libro.getPortada());
        hacerClicable(portada, () -> mostrarLibro(libro));
        Span nombre = new Span(titulo);
        nombre.addClassName("book-title");
        hacerClicable(nombre, () -> mostrarLibro(libro));
        Span autor = new Span(libro.getAutor() != null ? libro.getAutor() : "");
        autor.addClassName("book-author");
        Button ver = new Button("Ver libro", VaadinIcon.BOOK.create(), e -> mostrarLibro(libro));
        ver.addClassName("btn-reservar");
        ver.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Div tarjeta = new Div(portada, nombre, autor, ver);
        tarjeta.addClassName("book-card");
        return tarjeta;
    }

    private void hacerClicable(Component componente, Runnable accion) {
        componente.getElement().setAttribute("role", "button");
        componente.getElement().setAttribute("tabindex", "0");
        componente.getElement().addEventListener("click", e -> accion.run());
        componente.getElement().addEventListener("keydown", e -> accion.run()).setFilter("event.key === 'Enter'");
    }

    @SuppressWarnings("unchecked")
    private Set<Long> favoritos() {
        VaadinSession sesion = VaadinSession.getCurrent();
        Set<Long> favoritos = (Set<Long>) sesion.getAttribute("bookly.favoritos");
        if (favoritos == null) {
            favoritos = new HashSet<>();
            sesion.setAttribute("bookly.favoritos", favoritos);
        }
        return favoritos;
    }

    private boolean mismaCategoria(Libro a, Libro b) {
        return a.getCategoria() != null && b.getCategoria() != null
            && a.getCategoria().getId() != null
            && a.getCategoria().getId().equals(b.getCategoria().getId());
    }

    private void mostrarLibro(Libro libro) {
        pagina.removeAll();
        pagina.add(crearDetalleLibro(libro));
        pagina.getElement().executeJs("this.closest('.main-content').scrollTop = 0");
    }

    private Div crearFilaDetalle(String etiqueta, String valor) {
        Span e = new Span(etiqueta);
        e.addClassName("dato-label");
        Span v = new Span(valor);
        v.addClassName("dato-valor");
        Div fila = new Div(e, v);
        fila.addClassName("fila-detalle");
        return fila;
    }

    private Component crearDetalleLibro(Libro libro) {
        String titulo = libro.getTitulo() != null ? libro.getTitulo() : "Sin título";
        String autor = libro.getAutor() != null && !libro.getAutor().isBlank() ? libro.getAutor() : "Autor desconocido";
        String categoria = libro.getCategoria() != null && libro.getCategoria().getNombreCategoria() != null
            ? libro.getCategoria().getNombreCategoria() : "Sin categoría";
        String editorial = libro.getEditorial() != null && libro.getEditorial().getNombreEditorial() != null
            ? libro.getEditorial().getNombreEditorial() : "Sin editorial";

        List<DetalleReserva> reservasLibro = crudService.listarTodos(DetalleReserva.class).stream()
            .filter(d -> d.getLibro() != null && libro.getId() != null && libro.getId().equals(d.getLibro().getId()))
            .toList();
        List<Libro> otros = crudService.listarTodos(Libro.class).stream()
            .filter(l -> l.getId() != null && !l.getId().equals(libro.getId()))
            .toList();

        Button volver = new Button("Volver", VaadinIcon.ARROW_LEFT.create(), e -> irA(paginaActual));
        volver.addClassName("link-all");
        volver.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Div portada = crearPortada(titulo, libro.getPortada());
        portada.addClassName("grande");
        Span insignia = new Span(categoria);
        insignia.addClassName("badge-categoria");
        portada.add(insignia);
        Div colPortada = new Div(portada);
        colPortada.addClassName("detalle-portada");

        Div chipCategoria = new Div(VaadinIcon.TAG.create(), new Span(categoria));
        chipCategoria.addClassName("chip-categoria");
        H2 tituloLibro = new H2(titulo);
        tituloLibro.addClassName("detalle-titulo");
        Span nombreAutor = new Span(autor);
        nombreAutor.addClassName("detalle-autor");
        Div lineaAutor = new Div(new Span("por "), nombreAutor);
        lineaAutor.addClassName("detalle-por");
        Div datos = new Div(
            crearDato(VaadinIcon.BUILDING, "Editorial", editorial),
            crearDato(VaadinIcon.TAGS, "Categoría", categoria),
            crearDato(VaadinIcon.CLIPBOARD_TEXT, "Reservas", String.valueOf(reservasLibro.size()))
        );
        datos.addClassName("detalle-datos");
        Span descripcion = new Span(titulo + " es una obra de " + autor + " de la categoría " + categoria
            + ", publicada por la editorial " + editorial + ". Puedes reservarla desde esta página.");
        descripcion.addClassName("detalle-texto");
        Div info = new Div(chipCategoria, tituloLibro, lineaAutor, datos, descripcion);
        info.addClassName("detalle-info");

        Div lineaEstado = new Div(new Span("Disponible para reservar"));
        lineaEstado.addClassName("linea-estado");
        Button reservar = new Button("Reservar Libro", VaadinIcon.BOOKMARK.create(), e -> reservar(libro));
        reservar.addClassName("btn-reservar-grande");
        reservar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Set<Long> favs = favoritos();
        Button favorito = new Button();
        favorito.addClassName("btn-favorito");
        Runnable pintarFavorito = () -> {
            boolean esFavorito = favs.contains(libro.getId());
            favorito.setText(esFavorito ? "Quitar de Favoritos" : "Agregar a Favoritos");
            favorito.setIcon((esFavorito ? VaadinIcon.HEART : VaadinIcon.HEART_O).create());
        };
        favorito.addClickListener(e -> {
            if (!favs.remove(libro.getId())) {
                favs.add(libro.getId());
                Notification.show("Agregado a favoritos");
            } else {
                Notification.show("Quitado de favoritos");
            }
            pintarFavorito.run();
        });
        pintarFavorito.run();

        Span etiquetaPrestamo = new Span("Tiempo de préstamo");
        Span valorPrestamo = new Span(DIAS_PRESTAMO + " días");
        valorPrestamo.addClassName("dato-valor");
        Div lineaPrestamo = new Div(VaadinIcon.CLOCK.create(), etiquetaPrestamo, valorPrestamo);
        lineaPrestamo.addClassName("linea-prestamo");
        Div tarjetaReserva = new Div(lineaEstado, reservar, favorito, lineaPrestamo);
        tarjetaReserva.addClassName("tarjeta-lateral");

        Div tituloInteres = new Div(VaadinIcon.STAR.create(), new Span("También te puede interesar"));
        tituloInteres.addClassName("titulo-lateral");
        Div tarjetaInteres = new Div(tituloInteres);
        tarjetaInteres.addClassName("tarjeta-lateral");
        List<Libro> delAutor = libro.getAutor() == null ? List.<Libro>of() : otros.stream()
            .filter(l -> libro.getAutor().equalsIgnoreCase(l.getAutor()))
            .limit(3)
            .toList();
        if (delAutor.isEmpty()) {
            Span vacio = new Span("Explora más libros en el catálogo.");
            vacio.addClassName("book-author");
            tarjetaInteres.add(vacio);
        }
        for (Libro otro : delAutor) {
            Span enlace = new Span(otro.getTitulo());
            enlace.addClassName("enlace-libro");
            hacerClicable(enlace, () -> mostrarLibro(otro));
            tarjetaInteres.add(enlace);
        }
        Div lateral = new Div(tarjetaReserva, tarjetaInteres);
        lateral.addClassName("detalle-lateral");

        Div principal = new Div(colPortada, info, lateral);
        principal.addClassName("detalle");

        Div contenidoPestana = new Div();
        contenidoPestana.addClassName("contenido-pestana");
        Div pestanaDetalles = new Div(new Span("Detalles técnicos"));
        pestanaDetalles.addClassNames("chip", "selected");
        Div pestanaReservas = new Div(new Span("Reservas (" + reservasLibro.size() + ")"));
        pestanaReservas.addClassName("chip");
        Runnable verDetalles = () -> {
            contenidoPestana.removeAll();
            contenidoPestana.add(
                crearFilaDetalle("ID", String.valueOf(libro.getId())),
                crearFilaDetalle("Título", titulo),
                crearFilaDetalle("Autor", autor),
                crearFilaDetalle("Editorial", editorial),
                crearFilaDetalle("Categoría", categoria),
                crearFilaDetalle("Portada", libro.getPortada() != null && !libro.getPortada().isBlank() ? "Con imagen" : "Portada de color")
            );
            pestanaDetalles.addClassName("selected");
            pestanaReservas.removeClassName("selected");
        };
        Runnable verReservas = () -> {
            contenidoPestana.removeAll();
            if (reservasLibro.isEmpty()) {
                contenidoPestana.add(crearVacio("Este libro aún no tiene reservas."));
            }
            reservasLibro.forEach(d -> {
                String notas = d.getNotas() != null && !d.getNotas().isBlank() ? " - " + d.getNotas() : "";
                contenidoPestana.add(crearFilaDetalle("Reserva N° " + d.getReservaId(), "Cantidad: " + d.getCantidad() + notas));
            });
            pestanaReservas.addClassName("selected");
            pestanaDetalles.removeClassName("selected");
        };
        pestanaDetalles.addClickListener(e -> verDetalles.run());
        pestanaReservas.addClickListener(e -> verReservas.run());
        verDetalles.run();
        Div pestanas = new Div(pestanaDetalles, pestanaReservas);
        pestanas.addClassName("pestanas");
        Div panelPestanas = new Div(pestanas, contenidoPestana);
        panelPestanas.addClassName("panel");

        Button verTodos = new Button("Ver todos", VaadinIcon.ARROW_RIGHT.create(), e -> irA(1));
        verTodos.setIconAfterText(true);
        verTodos.addClassName("link-all");
        verTodos.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Div cabeceraGustar = new Div(new Span("También te puede gustar"), verTodos);
        cabeceraGustar.addClassName("panel-title");
        Div listaGustar = new Div();
        listaGustar.addClassName("gustar-lista");
        List<Libro> similares = otros.stream()
            .sorted(Comparator.comparing((Libro l) -> !mismaCategoria(libro, l)))
            .limit(3)
            .toList();
        if (similares.isEmpty()) {
            listaGustar.add(crearVacio("Aún no hay otros libros registrados."));
        }
        for (Libro similar : similares) {
            String tituloSimilar = similar.getTitulo() != null ? similar.getTitulo() : "Sin título";
            Div mini = crearPortada(tituloSimilar, similar.getPortada());
            mini.addClassName("media");
            Span nombreSimilar = new Span(tituloSimilar);
            nombreSimilar.addClassName("book-title");
            Span autorSimilar = new Span(similar.getAutor() != null ? similar.getAutor() : "");
            autorSimilar.addClassName("book-author");
            Div tarjetaMini = new Div(mini, nombreSimilar, autorSimilar);
            tarjetaMini.addClassName("mini-libro");
            hacerClicable(tarjetaMini, () -> mostrarLibro(similar));
            listaGustar.add(tarjetaMini);
        }
        Div panelGustar = new Div(cabeceraGustar, listaGustar);
        panelGustar.addClassName("panel");

        Div inferior = new Div(panelPestanas, panelGustar);
        inferior.addClassName("detalle-inferior");

        Div vista = new Div(volver, principal, inferior);
        vista.addClassName("view");
        return vista;
    }

    private Div crearPortada(String titulo, String imagen) {
        int i = Math.abs(titulo.hashCode() % PORTADAS.length);
        String url = imagen == null ? "" : imagen.trim();
        boolean hayImagen = url.startsWith("https://") || url.startsWith("http://") || url.startsWith("portadas/");
        Div portada = hayImagen ? new Div() : new Div(new Span(titulo));
        portada.addClassName("book-cover");
        if (hayImagen) {
            portada.getStyle().set("background",
                "url('" + url.replace("'", "%27") + "') center / cover no-repeat, " + PORTADAS[i]);
        } else {
            if (i == 2 || i == 5) {
                portada.addClassName("light");
            }
            portada.getStyle().set("background", PORTADAS[i]);
        }
        return portada;
    }

    private Div crearVacio(String mensaje) {
        Div vacio = new Div(new Span(mensaje));
        vacio.addClassName("vacio");
        return vacio;
    }

    private boolean contiene(String texto, String consulta) {
        return texto != null && texto.toLowerCase().contains(consulta);
    }

    private void reservar(Libro libro) {
        try {
            Reservas reserva = new Reservas("Reserva de " + libro.getTitulo(), "Reservado desde el catálogo");
            crudService.guardar(reserva);
            crudService.guardar(new DetalleReserva(reserva.getId(), libro, 1, ""));
            Notification.show("Libro reservado: " + libro.getTitulo());
        } catch (Exception ex) {
            Notification.show("No se pudo reservar el libro");
        }
    }

    // ==========================================
    // ACCESO DE ADMINISTRACIÓN
    // ==========================================
    private boolean adminActivo() {
        return Boolean.TRUE.equals(VaadinSession.getCurrent().getAttribute("bookly.admin"));
    }

    private void actualizarAdmin() {
        boolean activo = adminActivo();
        menuAdmin.setVisible(activo);
        if (activo) {
            addClassName("admin-on");
        } else {
            removeClassName("admin-on");
        }

        Icon llave = (activo ? VaadinIcon.UNLOCK : VaadinIcon.LOCK).create();
        Div circulo = new Div(llave);
        circulo.addClassName("gate-icon");
        Span titulo = new Span(activo ? "Modo administrador" : "Zona de administración");
        titulo.addClassName("gate-title");
        Span detalle = new Span(activo ? "Tienes acceso a la gestión completa" : "Solo personal autorizado");
        detalle.addClassName("gate-text");

        Button boton = new Button(activo ? "Cerrar sesión" : "Ingresar con clave",
            (activo ? VaadinIcon.SIGN_OUT : VaadinIcon.KEY).create(), e -> {
                if (adminActivo()) {
                    VaadinSession.getCurrent().setAttribute("bookly.admin", null);
                    actualizarAdmin();
                    irA(0);
                } else {
                    pedirClave(() -> irA(3));
                }
            });
        boton.addClassName("btn-gate");
        boton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        puertaAdmin.removeAll();
        puertaAdmin.add(circulo, titulo, detalle, boton);
        puertaAdmin.setClassName(activo ? "gate unlocked" : "gate");
    }

    private void pedirClave(Runnable alExito) {
        long ahora = System.currentTimeMillis();
        if (ahora < bloqueadoHasta) {
            Notification.show("Demasiados intentos. Espera " + ((bloqueadoHasta - ahora) / 1000 + 1) + " segundos.");
            return;
        }

        Dialog dialogo = new Dialog();
        dialogo.setClassName("admin-dialog");

        Div circulo = new Div(VaadinIcon.KEY.create());
        circulo.addClassName("dialog-icon");
        H3 titulo = new H3("Acceso de administrador");
        Span texto = new Span("Ingresa la clave para gestionar usuarios, libros y reservas.");
        texto.addClassName("dialog-text");
        PasswordField clave = new PasswordField("Clave");
        clave.setWidthFull();
        clave.setAutofocus(true);

        Div cuerpo = new Div(circulo, titulo, texto, clave);
        cuerpo.addClassName("dialog-body");
        dialogo.add(cuerpo);

        Button cancelar = new Button("Cancelar", e -> dialogo.close());
        Button ingresar = new Button("Ingresar");
        ingresar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ingresar.addClickShortcut(Key.ENTER);
        ingresar.addClickListener(e -> {
            boolean correcta = MessageDigest.isEqual(
                pinAdmin.getBytes(StandardCharsets.UTF_8),
                clave.getValue().getBytes(StandardCharsets.UTF_8));
            if (correcta) {
                intentosFallidos = 0;
                VaadinSession.getCurrent().setAttribute("bookly.admin", Boolean.TRUE);
                dialogo.close();
                actualizarAdmin();
                alExito.run();
                return;
            }
            intentosFallidos++;
            clave.clear();
            if (intentosFallidos >= 3) {
                intentosFallidos = 0;
                bloqueadoHasta = System.currentTimeMillis() + 30000;
                dialogo.close();
                Notification.show("Demasiados intentos. Espera 30 segundos.");
            } else {
                clave.setInvalid(true);
                clave.setErrorMessage("Clave incorrecta. Intentos restantes: " + (3 - intentosFallidos));
            }
        });
        dialogo.getFooter().add(cancelar, ingresar);
        dialogo.addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                dialogo.getElement().removeFromParent();
            }
        });
        dialogo.open();
    }
}