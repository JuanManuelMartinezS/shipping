// importación estática de módulos necesarios
// los import locales de JS tienen rutas relativas a la ruta del script que hace el enrutamiento
import * as Popper from '../utils/popper/popper.min.js'
import * as bootstrap from '../utils/bootstrap-5.3.3/js/bootstrap.bundle.min.js'
import { TabulatorFull as Tabulator } from '../utils/tabulator-6.3/js/tabulator_esm.min.js'
import { DateTime, Duration } from '../utils/luxon3x.min.js'
import icons from '../utils/own/icons.js'
import Helpers from '../utils/own/helpers.js'
import Popup from '../utils/own/popup.js'
import Toast from '../utils/own/toast.js'

class App {
  static async main() {
    // los recursos locales usan rutas relativas empezando por la carpeta principal del proyecto
    const config = await Helpers.fetchJSON('./resources/assets/config.json')
    // evite siempre los datos quemados en el código
    window.urlAPI = config.url
    // Ver: https://javascript.info/browser-environment (DOM|BOM|JavaScript)
    // Las clases importadas se asignan a referencias de la ventana actual:
    window.icons = icons
    window.DateTime = DateTime
    window.formatDateTime = {
      inputFormat: 'iso', // "yyyy-MM-dd'T'HH:mm:ss'Z'",  "yyyy-MM-dd'T'HH:mm:ss'Z'
      outputFormat: 'yyyy-MM-dd hh:mm a',
      invalidPlaceholder: 'fecha inválida',
    }
    window.Duration = Duration
    window.Helpers = Helpers
    window.Tabulator = Tabulator
    window.Toast = Toast
    window.Modal = Popup
    window.current = null // miraremos si se requiere...
    // lo siguiente es para estandarizar el estilo de los botones usados para add, edit y delete en las tablas
    window.addRowButton = `<button id='add-row' class='btn btn-info btn-sm'>${icons.plusSquare}&nbsp;&nbsp;New registration</button>`
    window.editRowButton = () => `<button id="edit-row" class="border-0 bg-transparent" data-bs-toggle="tooltip" title="Edit">${icons.edit}</button>`
    window.deleteRowButton = () => `<button id="delete-row" class="border-0 bg-transparent" data-bs-toggle="tooltip" title="Delete">${icons.delete}</button>`
    // lo siguiente es para estandarizar los botones de los formularios
    window.addButton = `${icons.plusSquare}&nbsp;&nbsp;<span>Add</span>`
    window.editButton = `${icons.editWhite}&nbsp;&nbsp;<span>Update</span>`
    window.deleteButton = `${icons.deleteWhite}<span>Delete</span>`
    window.cancelButton = `${icons.xLg}<span>Cancel</span>`
    window.tableHeight = 'calc(100vh - 190px)' // la altura de todos los elementos de tipo Tabulator que mostrará la aplicación

    try {
      // confirmación de acceso a la API REST
      const response = await Helpers.fetchJSON(`${urlAPI}/`)
      if (response.message === 'ok') {
        Toast.show({ title: 'Hi', message: response.data, duration: 1000 })
        App.#mainMenu()
      } else {
        Toast.show({ message: 'Problems with the data server', mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Falló la conexión con el servidor de datos', mode: 'danger', error: e })
    }
  }

  /**
   * Determina la acción a llevar a cabo según la opción elegida en el menú principal
   * @param {String} option El texto del ancla seleccionada
   */
  static async #mainMenu() {
    // referenciar todos los elementos <a>...</a> que hayan dentro de main-menu
    const listOptions = document.querySelectorAll('#main-menu a')
    document.querySelector('main').innerHTML = await Helpers.fetchText('./resources/html/home.html')
    // asignarle un gestor de evento clic a cada opción del menú
    listOptions.forEach(item =>
      item.addEventListener('click', async e => {
        let option = ''
        try {
          e.preventDefault()
          // asignar a option el texto de la opción del menú elegida
          option = e.target.text.trim() // <-- Importante!!!

          switch (option) {
            case 'Home':
              const { default: Home } = await import('./home.mjs')
              Home.init()
              break
            case 'Clients':
              // importar dinámicamente el módulo clientes.mjs
              const { default: Clients } = await import('./clients.mjs')
              Clients.init()
              break
            case 'Merchandises':
              // importar dinámicamente el módulo mercancias.mjs
              const { default: Merchandises } = await import('./merchandises.mjs')
              Merchandises.init()
              break
            case 'Packs':
              const { default: Packs } = await import('./shipments.mjs')
              Packs.init('pack')
              break
            case 'Envelopes':
              const { default: Envelopes } = await import('./shipments.mjs')
              Envelopes.init('envelope')
              break
            case 'Sacks':
              const { default: Sacks } = await import('./shipments.mjs')
              Sacks.init('sack')
              break

            case 'Boxes':
              const { default: Boxes } = await import('./boxes.mjs')
              Boxes.init()
              break
            case 'Statuses':
              const { default: Statuses } = await import('./statuses.mjs')
              Statuses.init()
              break
            case 'About':
              const { default: About } = await import('./about.mjs')
              About.init()
              break

            default:
              if (option !== 'Shipments') {
                Toast.show({ message: `The option ${option} has not been implemented`, mode: 'warning', delay: 3000, close: false })
                console.warn('Fallo en ', e.target)
              }
          }
        } catch (e) {
          Toast.show({
            message: `Failed to load the module ${option}`,
            mode: 'danger',
            error: e,
          })
        }
        return true
      })
    )
  }
}

App.main()
