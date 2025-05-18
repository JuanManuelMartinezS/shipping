export default class About {
  static #form
  constructor() {
    throw new Error('No requiere instancias, todos los métodos son estáticos. Use About.init()')
  }
  static async init() {
    try {
      // Cargar el formulario y agregarlo al <main>
      About.#form = await Helpers.fetchText('./resources/html/about.html')
      document.querySelector('main').innerHTML = About.#form
    } catch (error) {
      Toast.show({ title: 'About', message: 'Failure of the implementation load', mode: 'danger', error: e })
    }
  }
}
