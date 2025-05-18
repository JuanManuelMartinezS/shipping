export default class Home {
  static #form
  constructor() {
    throw new Error('No requiere instancias, todos los métodos son estáticos. Use Home.init()')
  }
  static async init() {
    try {
      // Cargar el formulario y agregarlo al <main>
      Home.#form = await Helpers.fetchText('./resources/html/home.html')
      document.querySelector('main').innerHTML = Home.#form
    } catch (error) {
      Toast.show({ title: 'Home', message: 'Failure of the implementation load', mode: 'danger', error: e })
    }
  }
}
