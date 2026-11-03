package com.example;


import com.example.model.Reservas;
import com.example.model.Usuarios;
import com.example.model.Libro;
import com.example.model.Editorial;
import com.example.model.Categoria;
import com.example.model.DetalleReserva;
import com.example.model.DetalleReservaId;
import com.vaadin.flow.component.textfield.IntegerField;
import com.example.service.CrudService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("sistema de reservas de libros")
@Route("")
public class MainView extends VerticalLayout {

    private final CrudService crudService;

    public MainView(CrudService crudService) {
        this.crudService = crudService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        TabSheet tabs = new TabSheet();
        tabs.setWidthFull();
        tabs.add("Usuarios", crearSeccionUsuarios());
        tabs.add("Reservas", crearSeccionReservas());
        tabs.add("Libros", crearSeccionLibros());
        tabs.add("Editoriales", crearSeccionEditoriales());
        tabs.add("Categorías", crearSeccionCategorias());
        tabs.add("Detalle Reserva", crearSeccionDetalleReserva());
            

        add(new H2("Bookly"), tabs);
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
    ComboBox<Editorial> editorialBox = new ComboBox<>("Editorial");
    ComboBox<Categoria> categoriaBox = new ComboBox<>("Categoría");

    editorialBox.setItems(crudService.listarTodos(Editorial.class));
    editorialBox.setItemLabelGenerator(Editorial::getNombreEditorial);
    categoriaBox.setItems(crudService.listarTodos(Categoria.class));
    categoriaBox.setItemLabelGenerator(Categoria::getNombreCategoria);

    FormLayout form = new FormLayout(idField, tituloField, autorField, editorialBox, categoriaBox);

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
        limpiar(idField, tituloField, autorField);
        editorialBox.clear();
        categoriaBox.clear();
        Notification.show("Libro eliminado");
    });
    btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

    Button btnLimpiar = new Button("Limpiar", e -> {
        limpiar(idField, tituloField, autorField);
        editorialBox.clear();
        categoriaBox.clear();
        grid.asSingleSelect().clear();
    });

    layout.add(form, crearAcciones(btnCrear, btnConsultar, btnActualizar, btnEliminar, btnLimpiar), grid);
    return layout;
}

    // ==========================================
    // 4. editorial
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
}
