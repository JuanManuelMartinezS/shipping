export default class Boxes {
  static #table
  static #modal
  static #currentOption
  static #form
  static #customers

  constructor() {
    throw new Error('No requiere instancias, todos los métodos son estáticos. Use Boxes.init()')
  }

  static async init() {
    try {
      Boxes.#form = await Helpers.fetchText('./resources/html/boxes.html')
      let response = await Helpers.fetchJSON(`${urlAPI}/client`)
      if (response.message != 'ok') {
        throw new Error(`Error in response: ${JSON.stringify(response)}`) // JavaScript no cuenta con una clase Exception
      }
      // crear las opciones para un select de clientes
      Boxes.#customers = Helpers.toOptionList({
        items: response.data,
        value: 'id',
        text: 'name',
        firstOption: 'Select a client',
      })
      response = await Helpers.fetchJSON(`${urlAPI}/box`)
      if (response.message != 'ok') {
        throw new Error(response.message)
      }

      //Obtener la lista de estados
      const response2 = await Helpers.fetchJSON(`${urlAPI}/delivery/statuses`)

      if (response2.message != 'ok') {
        throw new Error(response.message)
      }
      const listStatuses = response2.data

      // agregar al <main> de index.html la capa que contendrá la tabla
      document.querySelector('main').innerHTML = `
          <div class="p-2 w-full">
               <div id="table-container" class="m-2"></div>
          </dv>`
      Boxes.#table = new Tabulator('#table-container', {
        height: 'auto', // establecer la altura para habilitar el DOM virtual y mejorar la velocidad de procesamiento
        data: response.data,
        layout: 'fitDataTable', // ajustar columnas al ancho disponible. También fitData|fitDataFill|fitDataStretch|fitDataTable|fitColumns
        columns: [
          // definir las columnas de la tabla, para tipos datetime se utiliza formatDateTime definido en index.mjs
          { formatter: editRowButton, width: 40, hozAlign: 'center', cellClick: Boxes.#editRowClick },
          { formatter: deleteRowButton, width: 40, hozAlign: 'center', cellClick: Boxes.#deleteRowClick },
          { title: 'Number Guide', field: 'numGuide', hozAlign: 'center', width: 130 },
          { title: 'Content', field: 'content', width: 220 },
          { title: 'Sender', field: 'sender.name', width: 200 },
          { title: 'Addressee', field: 'addressee.name', hozAlign: 'center', width: 190 },
          { title: 'Value', field: 'value', hozAlign: 'center', width: 120 },
          { title: 'Payment', field: 'payment', hozAlign: 'center', width: 120 },
          { title: 'Width', field: 'width', hozAlign: 'center', visible: false },
          { title: 'Height', field: 'height', hozAlign: 'center', visible: false },
          { title: 'Length', field: 'length', hozAlign: 'center', visible: false },
          {
            title: 'Fragile',
            field: 'isFragile',
            hozAlign: 'center',
            formatter: 'tickCross',
            width: 90,
          },
          { title: 'Weight', field: 'weight', hozAlign: 'center', width: 90 },
          {
            title: 'Status',
            field: 'statuses',

            formatter: function (cell) {
              const lista = cell.getValue() //Obtiene la lista que llega a la celda
              if (Array.isArray(lista) && lista.length > 0) {
                const lastStatus = lista[lista.length - 1] //Obtiene el ultimo elemento de la lista
                const dateStr = lastStatus.dateTime // las fechas al ser de tipo localDateTime vienen asi "2023-11-09T14:30:00"
                const date = DateTime.fromISO(dateStr)
                const status = listStatuses.find(current => current.key === lastStatus.deliveryStatus)

                // Formatea la fecha
                const formatedDate = date.toFormat('yyyy-MM-dd hh:mm')

                return `${status.value} ${formatedDate}`
              } else {
                return '' // Retorna vacío si la lista está vacía o no es un array
              }
            },
            hozAlign: 'center',
            width: 250,
          },
        ],
        responsiveLayout: false, // activado el scroll horizontal, también: ['hide'|true|false]
        // mostrar al final de la tabla un botón para agregar registros
        footerElement: `<div class='container-fluid d-flex justify-content-end p-0'>${addRowButton}</div>`,
      })
      Boxes.#table.on('tableBuilt', () => document.querySelector('#add-row').addEventListener('click', Boxes.#addRow))
    } catch (e) {
      Toast.show({ title: 'Boxes', message: 'Failure of the implementation load', mode: 'danger', error: e })
    }

    return this
  }
  static async #addRow() {
    Boxes.#currentOption = 'add'
    Boxes.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Entry of Boxes</h5>',
      content: Boxes.#form,
      buttons: [
        { caption: addButton, classes: 'btn btn-primary me-2', action: () => Boxes.#add() },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Boxes.#modal.close() },
      ],
      doSomething: Boxes.#displayDataOnForm,
    })
    Boxes.#modal.show()
  }

  static async #add() {
    try {
      // verificar si los datos cumplen con las restricciones indicadas en el formulario HTML
      if (!Helpers.okForm('#form-boxes', Boxes.#otherValidations)) {
        return
      }
      // obtener del formulario el objeto con los datos que se envían a la solicitud POST
      const body = Boxes.#getFormData()

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(`${urlAPI}/box`, {
        method: 'POST',
        body,
      })

      if (response.message === 'ok') {
        Boxes.#table.addRow(response.data) // agregar la mercancía a la tabla
        Boxes.#modal.remove()
        Toast.show({ message: 'Successfully added' })
      } else {
        Toast.show({ message: 'Record could not be added', mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Failed record creation operation', mode: 'danger', error: e })
    }
  }
  static #editRowClick = async (e, cell) => {
    Boxes.#currentOption = 'edit'
    console.log(cell.getRow().getData())
    Boxes.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Update of Boxes</h5>',
      content: Boxes.#form,
      buttons: [
        { caption: addButton, classes: 'btn btn-primary me-2', action: () => Boxes.#edit(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Boxes.#modal.close() },
      ],
      doSomething: idModal => Boxes.#displayDataOnForm(idModal, cell.getRow().getData()),
    })
    Boxes.#modal.show()
  }

  static async #edit(cell) {
    try {
      // verificar si los datos cumplen con las restricciones indicadas en el formulario HTML
      if (!Helpers.okForm('#form-boxes', Boxes.#otherValidations)) {
        return
      }
      // obtener del formulario el objeto con los datos que se envían a la solicitud PATCH
      const body = Boxes.#getFormData()

      //Configurar la url para enviar la solicitud PATCH
      const url = `${urlAPI}/box/${cell.getRow().getData().id}`

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(url, {
        method: 'PATCH',
        body,
      })

      if (response.message === 'ok') {
        Toast.show({ message: 'Box information successfully updated' })
        cell.getRow().update(response.data)
        Boxes.#modal.remove()
      } else {
        Toast.show({ message: 'Unable to update box', mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Problems updating box', mode: 'danger', error: e })
    }
  }

  static #deleteRowClick = async (e, cell) => {
    Boxes.#currentOption = 'delete'
    console.log(cell.getRow().getData())
    Boxes.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Elimination of Boxes</h5>',
      content: `<span class="text-back dark:text-gray-300">Confirm the elimination of the box:<br>${cell.getRow().getData().id} - ${cell.getRow().getData().content}<br>Sender: ${cell.getRow().getData().sender.name}<br>Addressee: ${cell.getRow().getData().addressee.name}<br></span>`,
      buttons: [
        { caption: deleteButton, classes: 'btn btn-primary me-2', action: () => Boxes.#delete(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Boxes.#modal.close() },
      ],
    })
    Boxes.#modal.show()
  }

  static async #delete(cell) {
    try {
      //Configurar la url para enviar la solicitud DELETE
      const url = `${urlAPI}/box/${cell.getRow().getData().id}`

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(url, {
        method: 'DELETE',
      })

      if (response.message === 'ok') {
        Toast.show({ message: 'Box successfully deleted' })
        cell.getRow().delete()
        Boxes.#modal.remove()
      } else {
        Toast.show({ message: 'Could not delete the box', mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Problems deleting the box', mode: 'danger', error: e })
    }
  }

  static #toComplete(idModal, rowData) {
    console.warn('Sin implementar Boxes.toComplete()')
  }

  static async #displayDataOnForm(idModal, rowData) {
    // referenciar el select "cliente"
    const selectCustomers1 = document.querySelector(`#${idModal} #sender`)
    // asignar la lista de opciones al select "cliente" de Boxes.html
    selectCustomers1.innerHTML = Boxes.#customers
    const selectCustomers2 = document.querySelector(`#${idModal} #addressee`)
    selectCustomers2.innerHTML = Boxes.#customers
    //Para que cuando se vaya a actualizar los datos de un cliente en el formulario no aparezca vacio sino que aparezca ya los datos actuales
    if (Boxes.#currentOption === 'edit') {
      document.querySelector(`#${idModal} #content`).value = rowData.content
      document.querySelector(`#${idModal} #height`).value = rowData.height
      document.querySelector(`#${idModal} #width`).value = rowData.width
      document.querySelector(`#${idModal} #length`).value = rowData.length
      document.querySelector(`#${idModal} #weight`).value = rowData.weight
      document.querySelector(`#${idModal} #value`).value = rowData.value
      document.querySelector(`#${idModal} #isFragile`).checked = rowData.isFragile
      selectCustomers1.value = rowData.sender.id
      selectCustomers2.value = rowData.addressee.id
    }
  }

  // Las operaciones de creación y actualización de Boxes necesitan de una función auxiliar que permita obtener del formulario de adición/actualización, los datos necesarios para crear un objeto que se pueda utilizar como body de la petición.
  // Como puede ver, esta función lo único que hace es asignar a cada atributo del objeto que se retorna, uno de los valores que hay en los campos de entrada del formulario.
  static #getFormData() {
    // recuerde utilizar parseInt(), parseFloat() o Number() cuando sea necesario
    return {
      content: document.querySelector(`#${Boxes.#modal.id} #content`).value,
      weight: parseFloat(document.querySelector(`#${Boxes.#modal.id} #weight`).value),
      height: parseFloat(document.querySelector(`#${Boxes.#modal.id} #height`).value),
      width: parseFloat(document.querySelector(`#${Boxes.#modal.id} #width`).value),
      length: parseFloat(document.querySelector(`#${Boxes.#modal.id} #length`).value),
      isFragile: document.querySelector(`#${Boxes.#modal.id} #isFragile`).checked,
      value: parseFloat(document.querySelector(`#${Boxes.#modal.id} #value`).value),
      sender: document.querySelector(`#${Boxes.#modal.id} #sender`).value,
      addressee: document.querySelector(`#${Boxes.#modal.id} #addressee`).value,
    }
  }

  static #otherValidations() {
    //Referencie los elementos <select> sender y addressee
    const sender = document.querySelector(`#sender`)
    const addressee = document.querySelector(`#addressee`)

    if (sender.value === '') {
      Toast.show({ message: 'Sender selection is missing', mode: 'warning' })
      return false
    }

    if (addressee.value === '') {
      Toast.show({ message: 'Addressee selection is missing', mode: 'warning' })
      return false
    }

    if (sender.value === addressee.value) {
      Toast.show({ message: 'The addressee must be different from the sender.', mode: 'danger' })
      return false
    }

    if (document.querySelector(`#value`).value == 0) {
      Toast.show({ message: 'The estimated value cannot be zero', mode: 'warning' })
      return false
    }

    if (document.querySelector(`#weight`).value == 0) {
      Toast.show({ message: 'Weight cannot be zero', mode: 'warning' })
      return false
    }
    return true
  }
}
