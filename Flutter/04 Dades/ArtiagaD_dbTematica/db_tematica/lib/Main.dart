import 'package:flutter/material.dart'; // Paquete principal para construir interfaces en Flutter.
import 'package:http/http.dart' as http; // Paquete para hacer solicitudes HTTP.
import 'dart:convert'; // Para convertir datos JSON a Mapas y Listas.

void main() => runApp(
    const RocketLeagueDBApp()); // Punto de entrada de la app, ejecuta el widget principal.

class RocketLeagueDBApp extends StatelessWidget {
  const RocketLeagueDBApp({super.key}); // Constructor constante.

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(
          useMaterial3:
              true), // Define el tema de la app usando Material Design 3.
      debugShowCheckedModeBanner: false, // Oculta el banner de depuración.
      home: const HomePage(), // Establece la pantalla inicial como HomePage.
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  _HomePageState createState() =>
      _HomePageState(); // Crea el estado asociado a la pantalla principal.
}

typedef MenuEntry
    = DropdownMenuEntry<String>; // Tipo para entradas de menú desplegable.

class _HomePageState extends State<HomePage> {
  List<String> categories = []; // Lista de categorías obtenidas de la API.
  String?
      selectedCategory; // Categoría actualmente seleccionada por el usuario.
  List<Map<String, dynamic>> items =
      []; // Lista de ítems de la categoría seleccionada.
  Map<String, dynamic>?
      selectedItem; // Ítem seleccionado actualmente para mostrar detalles.

  @override
  void initState() {
    super.initState();
    fetchCategories(); // Llama a la función para obtener las categorías al inicio.
  }

  // Función que obtiene las categorías desde la API.
  Future<void> fetchCategories() async {
    final response = await http.get(Uri.parse(
        'http://localhost:3000/api/categories')); // Realiza la solicitud HTTP.

    if (response.statusCode == 200) {
      // Verifica que la respuesta sea exitosa.
      setState(() {
        categories = List<String>.from(json.decode(
            response.body)); // Decodifica y guarda las categorías en la lista.
        selectedCategory = categories.isNotEmpty
            ? categories.first
            : null; // Selecciona la primera categoría si existe.
        if (selectedCategory != null)
          fetchItems(selectedCategory!); // Carga los ítems de esa categoría.
      });
    } else {
      print(
          'Error fetching categories: ${response.statusCode}'); // Muestra un mensaje si ocurre un error.
    }
  }

  // Función que obtiene los ítems de una categoría seleccionada.
  Future<void> fetchItems(String category) async {
    final response = await http.get(Uri.parse(
        'http://localhost:3000/api/items/$category')); // Solicita ítems para la categoría.

    if (response.statusCode == 200) {
      setState(() {
        items = List<Map<String, dynamic>>.from(json.decode(
            response.body)); // Decodifica y guarda los ítems en la lista.
        selectedItem = null; // Limpia la selección previa de ítems.
      });
    } else {
      print(
          'Error fetching items: ${response.statusCode}'); // Muestra un mensaje si ocurre un error.
    }
  }

  // Función que obtiene los detalles de un ítem específico por su ID.
  Future<void> fetchItemDetails(int id) async {
    final response = await http.get(Uri.parse(
        'http://localhost:3000/api/items/id/$id')); // Solicita los detalles del ítem.

    if (response.statusCode == 200) {
      setState(() {
        selectedItem = json.decode(
            response.body); // Guarda los detalles del ítem seleccionado.
      });
    } else {
      print(
          'Error fetching item details: ${response.statusCode}'); // Muestra un mensaje si ocurre un error.
    }
  }

  @override
  Widget build(BuildContext context) {
    final isMobile = MediaQuery.of(context).size.width <
        600; // Determina si la pantalla es móvil o de escritorio.

    return Scaffold(
      appBar: AppBar(
        backgroundColor:
            Colors.deepPurple, // Define el color de fondo de la barra superior.
        title: const Text(
          'Rocket League DB', // Título de la app.
          style: TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
          textAlign: TextAlign.center, // Centra el texto del título.
        ),
        centerTitle: true, // Asegura que el título esté centrado.
      ),
      body: AnimatedSwitcher(
        duration: const Duration(
            milliseconds: 1500), // Duración de la animación (1.5 segundos).
        switchInCurve: Curves.easeInOut, // Curva de entrada para la animación.
        switchOutCurve: Curves.easeInOut, // Curva de salida para la animación.
        transitionBuilder: (Widget child, Animation<double> animation) {
          return FadeTransition(
            opacity:
                animation, // Efecto de desvanecimiento durante la transición.
            child: child,
          );
        },
        child: isMobile
            ? buildMobileLayout()
            : buildDesktopLayout(), // Muestra el diseño según el tamaño de la pantalla.
      ),
    );
  }

  // Diseño para pantallas móviles.
  Widget buildMobileLayout() {
    return SingleChildScrollView(
      child: Column(
        children: [
          Padding(
            padding:
                const EdgeInsets.symmetric(vertical: 16.0, horizontal: 8.0),
            child: DropdownMenu<String>(
              initialSelection:
                  selectedCategory, // Muestra la categoría seleccionada.
              onSelected: (String? value) {
                if (value != null) {
                  setState(() {
                    selectedCategory =
                        value; // Actualiza la categoría seleccionada.
                    fetchItems(
                        value); // Obtiene los ítems de la nueva categoría.
                  });
                }
              },
              dropdownMenuEntries: categories
                  .map((category) => DropdownMenuEntry<String>(
                        // Crea las opciones del menú desplegable.
                        value: category,
                        label: category,
                      ))
                  .toList(),
            ),
          ),
          Container(
            height: 200, // Altura del contenedor que muestra los ítems.
            color: Colors.grey[200], // Fondo gris claro.
            child: items.isEmpty
                ? const Center(
                    child: Text(
                        'No items found')) // Muestra un mensaje si no hay ítems.
                : ListView.separated(
                    itemCount: items.length, // Cantidad de ítems.
                    separatorBuilder: (context, index) => const Divider(
                        color: Colors.grey), // Línea separadora entre ítems.
                    itemBuilder: (context, index) {
                      final item = items[index]; // Ítem actual.
                      return ListTile(
                        title: Text(item['name'] ??
                            'Unknown Item'), // Muestra el nombre del ítem.
                        onTap: () => fetchItemDetails(item[
                            'id']), // Obtiene los detalles del ítem al pulsarlo.
                      );
                    },
                  ),
          ),
          const SizedBox(
              height: 16), // Espaciado entre la lista y los detalles.
          if (selectedItem != null) // Si hay un ítem seleccionado...
            Column(
              children: [
                Image.network(
                  'http://localhost:3000/${selectedItem!['photo']}', // Muestra la imagen del ítem.
                  height: 200,
                  fit: BoxFit.contain, // Ajusta la imagen al contenedor.
                ),
                const SizedBox(height: 16),
                Text(
                  selectedItem!['description'] ??
                      'No Description', // Muestra la descripción del ítem.
                  style: const TextStyle(
                    fontSize: 14,
                    color: Colors.deepPurple,
                    fontStyle: FontStyle.italic,
                  ),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          if (selectedItem == null) // Si no hay un ítem seleccionado...
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 16.0),
              child: Text(
                'Select an item to view details', // Mensaje indicando que seleccione un ítem.
                style: TextStyle(
                  fontSize: 16,
                  color: Colors.grey,
                ),
              ),
            ),
        ],
      ),
    );
  }

  // Diseño para pantallas de escritorio.
  Widget buildDesktopLayout() {
    return Row(
      children: [
        Flexible(
          flex: 2,
          child: Container(
            color: Colors.grey[200], // Fondo gris claro.
            child: items.isEmpty
                ? const Center(
                    child: Text(
                        'No items found')) // Muestra un mensaje si no hay ítems.
                : ListView.separated(
                    itemCount: items.length, // Cantidad de ítems.
                    separatorBuilder: (context, index) =>
                        const Divider(color: Colors.grey),
                    itemBuilder: (context, index) {
                      final item = items[index];
                      return ListTile(
                        title: Text(item['name'] ?? 'Unknown Item'),
                        onTap: () => fetchItemDetails(item['id']),
                      );
                    },
                  ),
          ),
        ),
        Flexible(
          flex: 3,
          child: selectedItem == null
              ? const Center(
                  child: Text(
                    'Select an item to view details',
                    style: TextStyle(fontSize: 16, color: Colors.grey),
                  ),
                )
              : SingleChildScrollView(
                  child: Center(
                    child: Column(
                      children: [
                        Image.network(
                          'http://localhost:3000/${selectedItem!['photo']}',
                          height: 256,
                          fit: BoxFit.contain,
                        ),
                        const SizedBox(height: 16),
                        Text(
                          selectedItem!['description'] ?? 'No Description',
                          style: const TextStyle(
                              fontSize: 14,
                              color: Colors.deepPurple,
                              fontStyle: FontStyle.italic),
                          textAlign: TextAlign.center,
                        ),
                      ],
                    ),
                  ),
                ),
        ),
      ],
    );
  }
}
