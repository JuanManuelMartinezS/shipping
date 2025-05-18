import Helpers from '../utils/own/helpers.js'
export default class Clients {
  static #table
  static #modal
  static #currentOption
  static #form
  static #customers
  static #cities

  constructor() {
    throw new Error('No requiere instancias, todos los métodos son estáticos. Use Clients.init()')
  }

  static async init() {
    try {
      Clients.#form = await Helpers.fetchText('./resources/html/clientes.html')
      let response = await Helpers.fetchJSON(`${urlAPI}/client`)
      if (response.message != 'ok') {
        throw new Error(response.message)
      }

      //Llama al json para crear el select de las ciudades
      Clients.#cities = Helpers.toOptionList({
        items: await Helpers.fetchJSON('./resources/assets/cities.json'),
        value: 'codigo',
        text: 'nombre',
      })

      // agregar al <main> de index.html la capa que contendrá la tabla
      document.querySelector('main').innerHTML = `
          <div class="p-2 w-full">
                 <div id="table-container" class="m-2"></div>
          </dv>`

      Clients.#table = new Tabulator('#table-container', {
        height: 'auto', // establecer la altura para habilitar el DOM virtual y mejorar la velocidad de procesamiento
        data: response.data,
        layout: 'fitDataTable', // ajustar columnas al ancho disponible. También fitData|fitDataFill|fitDataStretch|fitDataTable|fitColumns
        columns: [
          // definir las columnas de la tabla, para tipos datetime se utiliza formatDateTime definido en index.mjs
          { formatter: editRowButton, width: 40, hozAlign: 'center', cellClick: Clients.#editRowClick },
          { formatter: deleteRowButton, width: 40, hozAlign: 'center', cellClick: Clients.#deleteRowClick },
          { title: 'Id', field: 'id', hozAlign: 'center', width: 90 },
          { title: 'Name', field: 'name', width: 200 },
          { title: 'Address', field: 'address', width: 300 },
          { title: 'Phone', field: 'phoneNumber', hozAlign: 'center', width: 140 },
          { title: 'City', field: 'city', hozAlign: 'center', width: 170 },
        ],
        responsiveLayout: false, // activado el scroll horizontal, también: ['hide'|true|false]
        // mostrar al final de la tabla un botón para agregar registros
        footerElement: `<div class='container-fluid d-flex justify-content-end p-0'>${addRowButton}</div>`,
      })
      Clients.#table.on('tableBuilt', () => document.querySelector('#add-row').addEventListener('click', Clients.#addRow))
    } catch (e) {
      Toast.show({ title: 'Clients', message: 'Falló la carga de la información', mode: 'danger', error: e })
    }

    return this
  }
  static async #addRow() {
    Clients.#currentOption = 'add'
    Clients.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Entry of Clients</h5>',
      content: Clients.#form,
      buttons: [
        { caption: addButton, classes: 'btn btn-primary me-2', action: () => Clients.#add() },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Clients.#modal.close() },
      ],
      doSomething: Clients.#displayDataOnForm,
    })
    Clients.#modal.show()
  }

  static async #add() {
    try {
      // verificar si los datos cumplen con las restricciones indicadas en el formulario HTML
      if (!Helpers.okForm('#form-clientes')) {
        return
      }
      // obtener del formulario el objeto con los datos que se envían a la solicitud POST
      const body = Clients.#getFormData()

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(`${urlAPI}/client`, {
        method: 'POST',
        body,
      })

      if (response.message === 'ok') {
        Clients.#table.addRow(response.data) // agregar la mercancía a la tabla
        Clients.#modal.remove()
        Toast.show({ message: 'Successfully added' })
      } else {
        Toast.show({ message: response.message, mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Failed record creation operation', mode: 'danger', error: e })
    }
  }

  static #editRowClick = async (e, cell) => {
    Clients.#currentOption = 'edit'
    console.log(cell.getRow().getData())
    Clients.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Update of Clients</h5>',
      content: Clients.#form,
      buttons: [
        { caption: editButton, classes: 'btn btn-primary me-2', action: () => Clients.#edit(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Clients.#modal.close() },
      ],
      doSomething: idModal => Clients.#displayDataOnForm(idModal, cell.getRow().getData()),
    })
    Clients.#modal.show()
  }

  static async #edit(cell) {
    try {
      // verificar si los datos cumplen con las restricciones indicadas en el formulario HTML
      if (!Helpers.okForm('#form-clientes')) {
        return
      }
      // obtener del formulario el objeto con los datos que se envían a la solicitud PATCH
      const body = Clients.#getFormData()

      //Configurar la url para enviar la solicitud PATCH
      const url = `${urlAPI}/client/${cell.getRow().getData().id}`

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(url, {
        method: 'PATCH',
        body,
      })

      if (response.message === 'ok') {
        Toast.show({ message: 'Client information successfully updated' })
        cell.getRow().update(response.data)
        Clients.#modal.remove()
      } else {
        Toast.show({ message: 'Unable to update client', mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Problems updating the client', mode: 'danger', error: e })
    }
  }

  static #deleteRowClick = async (e, cell) => {
    Clients.#currentOption = 'delete'
    console.log(cell.getRow().getData())
    Clients.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Elimination of Clients</h5>',
      content: `<span class="text-back dark:text-gray-300">Confirm the elimination of the client:<br>${cell.getRow().getData().id}<br>Name: ${cell.getRow().getData().name}<br>Address: ${cell.getRow().getData().address}<br></span>`,
      buttons: [
        { caption: deleteButton, classes: 'btn btn-primary me-2', action: () => Clients.#delete(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Clients.#modal.close() },
      ],
    })
    Clients.#modal.show()
  }

  static async #delete(cell) {
    try {
      //Configurar la url para enviar la solicitud DELETE
      const url = `${urlAPI}/client/${cell.getRow().getData().id}`

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(url, {
        method: 'DELETE',
      })

      if (response.message === 'ok') {
        Toast.show({ message: 'Client successfully deleted' })
        cell.getRow().delete()
        Clients.#modal.remove()
      } else {
        Toast.show({ message: response.message, mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Problems deleting the client', mode: 'danger', error: e })
    }
  }

  static #toComplete(idModal, rowData) {
    console.warn('Sin implementar Clients.toComplete()')
  }

  static async #displayDataOnForm(idModal, rowData) {
    const selectCities = document.querySelector(`#${idModal} #city`)
    //Asignar la lista de opciones al select "city" de clients.html
    selectCities.innerHTML = Clients.#cities

    //Para que cuando se vaya a actualizar los datos de un cliente en el formulario no aparezca vacio sino que aparezca ya los datos actuales
    if (Clients.#currentOption === 'edit') {
      document.querySelector(`#${idModal} #id`).value = rowData.id
      document.querySelector(`#${idModal} #name`).value = rowData.name
      document.querySelector(`#${idModal} #address`).value = rowData.address
      document.querySelector(`#${idModal} #phoneNumber`).value = rowData.phoneNumber

      //Buscar el indice de la opcion cuyo indice sea igual al de la fila seleccionada
      Helpers.selectOptionByText(selectCities, rowData.city)
    }
  }

  /**
   * Las operaciones de creación y actualización de mercancias necesitan de una función auxiliar que permita obtener del formulario de adición/actualización, los datos necesarios para crear un objeto que se pueda utilizar como body de la petición, preste atencion a los metodos usados para acceder a la ciudad
   * @returns Un objeto con los datos del usuario
   */
  static #getFormData() {
    console.log(Clients.#modal.id)
    const city = document.querySelector(`#${Clients.#modal.id} #city`)
    console.log(city)
    const index = city.selectedIndex
    // recuerde utilizar parseInt(), parseFloat() o Number() cuando sea necesario
    return {
      id: document.querySelector(`#${Clients.#modal.id} #id`).value,
      name: document.querySelector(`#${Clients.#modal.id} #name`).value,
      address: document.querySelector(`#${Clients.#modal.id} #address`).value,
      phoneNumber: document.querySelector(`#${Clients.#modal.id} #phoneNumber`).value,
      city: city.options[index].text,
    }
  }
}
