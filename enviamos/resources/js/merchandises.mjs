export default class Merchandises {
  static #table
  static #modal
  static #currentOption
  static #form
  static #customers

  constructor() {
    throw new Error('No requiere instancias, todos los métodos son estáticos. Use Merchandises.init()')
  }

  static async init() {
    try {
      Merchandises.#form = await Helpers.fetchText('./resources/html/merchandises.html')

      // La primera instrucción intenta cargar la información de Merchandises, si la carga es correcta, se utiliza el método Helpers.toOptionList() para crear el con los Merchandises cargados una lista de opciones que más adelante se inyectarán a un <select
      let response = await Helpers.fetchJSON(`${urlAPI}/client`)
      if (response.message != 'ok') {
        throw new Error(`Error in response: ${JSON.stringify(response)}`) // JavaScript no cuenta con una clase Exception
      }
      console.log(response)

      // crear las opciones para un select de Merchandises
      Merchandises.#customers = Helpers.toOptionList({
        items: response.data,
        value: 'id',
        text: 'name',
        firstOption: 'Select a client',
      })
      response = await Helpers.fetchJSON(`${urlAPI}/merchandise`)

      // agregar al <main> de index.html la capa que contendrá la tabla
      document.querySelector('main').innerHTML = `
        <div class="p-2 w-full">
              <div id="table-container" class="m-2"></div>
        </div>`

      Merchandises.#table = new Tabulator('#table-container', {
        height: 'auto', // establecer la altura para habilitar el DOM virtual y mejorar la velocidad de procesamiento
        data: response.data,
        layout: 'fitDataTable', // ajustar columnas al width disponible. También fitData|fitDataFill|fitDataStretch|fitDataTable|fitColumns
        columns: [
          // definir las columnas de la tabla, para tipos datetime se utiliza formatDateTime definido en index.mjs
          { formatter: editRowButton, width: 40, hozAlign: 'center', cellClick: Merchandises.#editRowClick },
          { formatter: deleteRowButton, width: 40, hozAlign: 'center', cellClick: Merchandises.#deleteRowClick },
          { title: 'Id', field: 'id', hozAlign: 'center', width: 90 },
          { title: 'Storer', field: 'storer.name', width: 170 },
          { title: 'Says to contain', field: 'content', width: 300 },
          { title: 'Arrival', field: 'dayOfArrival', width: 190, formatter: 'datetime', formatterParams: formatDateTime },
          { title: 'Departure', field: 'dayOfDeparture', width: 190, formatter: 'datetime', formatterParams: formatDateTime },
          { title: 'Height', field: 'volume', hozAlign: 'center', visible: false },
          { title: 'Width', field: 'volume', hozAlign: 'center', visible: false },
          { title: 'Length', field: 'volume', hozAlign: 'center', visible: false },
          { title: 'Vol. m³', field: 'volume', hozAlign: 'center', width: 90 },
          { title: 'Payment', field: 'payment', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
          { title: 'Warehouse', field: 'warehouse', width: 280 },
        ],
        responsiveLayout: false, // activado el scroll horizontal, también: ['hide'|true|false]
        initialSort: [
          // establecer el ordenamiento inicial de los datos
          { column: 'dayOfArrival', dir: 'asc' },
        ],
        columnDefaults: {
          tooltip: true, //show tool tips on cells
        },

        // mostrar al final de la tabla un botón para agregar registros
        footerElement: `<div class='container-fluid d-flex justify-content-end p-0'>${addRowButton}</div>`,
      })
      // Los objetos de tipo Tabulator tienen varios gestores de eventos, entre ellos uno para el evento tableBuilt que permite asignar un callback que se encargue de llevar a cabo alguna acción.
      // En este caso se requiere que cuando la tabla haya sido creada, se agregue un gestor de eventos clic para el botón “Nuevo registro” que se muestra en la parte inferior de la tabla. Esto hace necesario agregar el siguiente código:

      Merchandises.#table.on('tableBuilt', () => document.querySelector('#add-row').addEventListener('click', Merchandises.#addRow))
    } catch (e) {
      Toast.show({ title: 'Merchandises', message: 'Failed to load information', mode: 'danger', error: e })
    }

    return this
  }

  /**
   * La instrucción new Modal({…}) crea una instancia de la clase Popup disponible en ./resources/utils/own/popup.js. Ver líneas 9 y 31 del módulo index.mjs.
  La propiedad classes del objeto dado como argumento del constructor, asigna estilos de Bootstrap a la ventana modal creada mediante new Modal({…}).
  La propiedad title asigna el título que mostrará la ventana modal.
  La propiedad content corresponde a lo que se mostrará en el cuerpo de la ventana modal.
  La propiedad buttons recibe un array de objetos. Cada uno de estos representa un botón que se muestra en la parte inferior de la ventana modal.
  A la propiedad doSomething se le debe asignar un callback, en este caso se le asigna como referencia el método del punto 12.
  La instrucción Merchandises.#modal.show(), muestra la ventana modal creada.
   */
  static async #addRow() {
    Merchandises.#currentOption = 'add'
    Merchandises.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Entry of merchandises</h5>',
      content: Merchandises.#form,
      buttons: [
        { caption: addButton, classes: 'btn btn-primary me-2', action: () => Merchandises.#add() },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Merchandises.#modal.close() },
      ],
      doSomething: Merchandises.#displayDataOnForm,
    })
    Merchandises.#modal.show()
  }

  //   Se empieza por verificar si los datos del formulario son correctos, con respecto a las validaciones establecidas en el formulario HTML.
  // Luego mediante un llamado a getFormData() se obtiene el body a utilizar en la petición y seguidamente se hace la petición POST.
  // Si la API retorna un mensaje de “ok”, se agrega el nuevo registro a la tabla mediante la operación addRow y se destruye del modal con el formulario, si sucede un error, se muestra en el navegador un mensaje para el usuario final y en la consola otro para el desarrollador.

  static async #add() {
    try {
      // verificar si los datos cumplen con las restricciones indicadas en el formulario HTML
      if (!Helpers.okForm('#form-merchandises', Merchandises.#otherValidations)) {
        return
      }
      // obtener del formulario el objeto con los datos que se envían a la solicitud POST
      const body = Merchandises.#getFormData()

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(`${urlAPI}/merchandise`, {
        method: 'POST',
        body,
      })

      if (response.message === 'ok') {
        Merchandises.#table.addRow(response.data) // agregar la mercancía a la tabla
        Merchandises.#modal.remove()
        Toast.show({ message: 'Successfully added' })
      } else {
        Toast.show({ message: 'Record could not be added', mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Failed record creation operation', mode: 'danger', error: e })
    }
  }

  /**
   * 
  Esta función recibe como argumento el ID del modal y tiene como única instrucción un llamado a la función displayDataOnForm(…) a la cual le pasa dos argumentos: el ID del modal y un objeto con la data de la fila a la cual pertenece la celda en la que se pulsó clic.

   * Se entiende entonces que el argumento cell que recibe el método editRowClick() corresponde a una instancia de la celda sobre la cual se da click y que dicha instancia permite acceder a la instancia de su fila mediante getRow() y que la instancia de la fila retorna un objeto con la data cuando se usa getData().
   */
  static #editRowClick = async (e, cell) => {
    Merchandises.#currentOption = 'edit'

    Merchandises.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Update of merchandises</h5>',
      content: Merchandises.#form,
      buttons: [
        { caption: editButton, classes: 'btn btn-primary me-2', action: () => Merchandises.#edit(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Merchandises.#modal.close() },
      ],
      doSomething: idModal => Merchandises.#displayDataOnForm(idModal, cell.getRow().getData()),
    })
    Merchandises.#modal.show()
  }

  //  Observe que la instancia de Cell recibida como argumento (la celda sobre la que se pulsa clic), permite obtener una referencia a su fila completa (getRow()) y que mediante getRow() se accede a los datos de la fila (getData()), para finalmente obtener el ID.

  static async #edit(cell) {
    try {
      // verificar si los datos cumplen con las restricciones indicadas en el formulario HTML
      if (!Helpers.okForm('#form-merchandises', Merchandises.#otherValidations)) {
        return
      }
      // obtener del formulario el objeto con los datos que se envían a la solicitud PATCH
      const body = Merchandises.#getFormData()

      //Configurar la url para enviar la solicitud PATCH
      const url = `${urlAPI}/merchandise/${cell.getRow().getData().id}`

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(url, {
        method: 'PATCH',
        body,
      })

      if (response.message === 'ok') {
        Toast.show({ message: 'Merchandise information successfully updated' })
        cell.getRow().update(response.data)
        Merchandises.#modal.remove()
      } else {
        Toast.show({ message: 'Unable to update merchandise', mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Problems updating the merchandise', mode: 'danger', error: e })
    }
  }

  /**
   *
   * La implementación del método es muy similar a la de los dos anteriores y el comportamiento también, sólo que a la propiedad content se le asigna aquí el HTML que se requiere para documentar y confirmar la eliminación del registro actual.
   *
   */
  static #deleteRowClick = async (e, cell) => {
    Merchandises.#currentOption = 'delete'
    console.log(cell.getRow().getData())
    Merchandises.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Elimination of merchandises</h5>',
      content: `<span class="text-back dark:text-gray-300">Confirm the elimination of the merchandise:<br>${cell.getRow().getData().id} - ${cell.getRow().getData().content}<br>Warehouse: ${cell.getRow().getData().warehouse}<br>Storer: ${cell.getRow().getData().storer.name}<br></span>`,
      buttons: [
        { caption: deleteButton, classes: 'btn btn-primary me-2', action: () => Merchandises.#delete(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Merchandises.#modal.close() },
      ],
    })
    Merchandises.#modal.show()
  }
  static async #delete(cell) {
    try {
      //Configurar la url para enviar la solicitud DELETE
      const url = `${urlAPI}/merchandise/${cell.getRow().getData().id}`

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(url, {
        method: 'DELETE',
      })

      if (response.message === 'ok') {
        Toast.show({ message: 'Merchandise successfully deleted' })
        cell.getRow().delete()
        Merchandises.#modal.close()
      } else {
        Toast.show({ message: 'Could not delete merchandise', mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Problems deleting the merchandise', mode: 'danger', error: e })
    }
  }
  /**
   *
   *  inyectar la lista de Merchandises al <select> del formulario de merchandises
   *
   */
  static #displayDataOnForm(idModal, rowData) {
    // referenciar el select mercancia"
    const selectCustomers = document.querySelector(`#${idModal} #storer`)
    // asignar la lista de opciones al selecla mercancia" de merchandises.html
    selectCustomers.innerHTML = Merchandises.#customers
    //Para que cuando se vaya a actualizar los datos de la mercancia en el formulario no aparezca vacio sino que aparezca ya los datos actuales
    if (Merchandises.#currentOption === 'edit') {
      document.querySelector(`#${idModal} #id`).value = rowData.id
      document.querySelector(`#${idModal} #content`).value = rowData.content
      document.querySelector(`#${idModal} #height`).value = rowData.height
      document.querySelector(`#${idModal} #width`).value = rowData.width
      document.querySelector(`#${idModal} #length`).value = rowData.length
      document.querySelector(`#${idModal} #arrival`).value = rowData.dayOfArrival
      document.querySelector(`#${idModal} #departure`).value = rowData.dayOfDeparture
      document.querySelector(`#${idModal} #warehouse`).value = rowData.warehouse
      selectCustomers.value = rowData.storer.id
    } else {
      //por defecto, asignar a ingreso del formulario la fecha y hora actual
      const now = DateTime.now()
      document.querySelector('#form-merchandises #arrival').value = now.toFormat('yyyy-MM-dd HH:mm')
      //por defecto, asignar a la salida la hora del ingreso mas una hora
      // Como DateTime es un objeto no se puede simplemente sumar +1 hora porque no se tendria en cuenta la fecha asi que usando la biblioteca luxon (por el uso de DateTime), se puede usar su método .plus() para sumar tiempo al objeto DateTime.
      const departure = now.plus({ hours: 1 })
      document.querySelector('#form-merchandises #departure').value = departure.toFormat('yyyy-MM-dd HH:mm')
    }
  }

  // Las operaciones de creación y actualización de Merchandises necesitan de una función auxiliar que permita obtener del formulario de adición/actualización, los datos necesarios para crear un objeto que se pueda utilizar como body de la petición.
  // Como puede ver, esta función lo único que hace es asignar a cada atributo del objeto que se retorna, uno de los valores que hay en los campos de entrada del formulario.
  static #getFormData() {
    const storer = document.querySelector(`#${Merchandises.#modal.id} #storer`)

    // recuerde utilizar parseInt(), parseFloat() o Number() cuando sea necesario
    return {
      id: document.querySelector(`#${Merchandises.#modal.id} #id`).value,
      storer: document.querySelector(`#${Merchandises.#modal.id} #storer`).value,
      content: document.querySelector(`#${Merchandises.#modal.id} #content`).value,
      height: parseFloat(document.querySelector(`#${Merchandises.#modal.id} #height`).value),
      width: parseFloat(document.querySelector(`#${Merchandises.#modal.id} #width`).value),
      length: parseFloat(document.querySelector(`#${Merchandises.#modal.id} #length`).value),
      dayOfArrival: document.querySelector(`#${Merchandises.#modal.id} #arrival`).value,
      dayOfDeparture: document.querySelector(`#${Merchandises.#modal.id} #departure`).value,
      warehouse: document.querySelector(`#${Merchandises.#modal.id} #warehouse`).value,
    }
  }

  static #otherValidations() {
    //Referencie los elementos <select> sender y addressee
    if (document.querySelector(`#storer`).value === '') {
      Toast.show({ message: 'Customer selection is missing', mode: 'warning' })
      return false
    }
    let dayOfArrival = new Date(document.querySelector(`#${Merchandises.#modal.id} #arrival`).value)
    let dayOfDeparture = new Date(document.querySelector(`#${Merchandises.#modal.id} #departure`).value)

    if (dayOfArrival > dayOfDeparture) {
      Toast.show({ title: 'Merchandises', message: 'Arrival date cannot be after Departure date', mode: 'danger' })
    }

    return true
  }
}
