export default class TestTools {
  constructor() {
    throw new Error('No requiere instancias, todos los métodos son estáticos. Use TestTools.init()')
  }

  static async init() {
    console.log('Iniciadas las pruebas de herramientas')

    // ejemplo de uso de los estándares definidos en index.mjs:
    let html = `
      <br>
      <a class="lead" href="https://es.javascript.info/browser-environment" target="_blank">
        <strong>Entorno del navegador, especificaciones<strong>
      </a>
      <br>
      <div class="my-3">
        ${editRowButton()}Editar ${deleteRowButton()}Eliminar
      </div>
      ${addRowButton}
      <br>
      <button id="agregar" class="btn btn-success btn-sm">${addButton}</button>
      <button id="actualizar" class="btn btn-warning btn-sm">${editButton}</button>
      <button id="cancelar" class="btn btn-secondary btn-sm">${cancelButton}</button>
      <button id="eliminar" class="btn btn-danger btn-sm">${deleteButton}</button>
    `
    document.querySelector('main').insertAdjacentHTML('beforeend', html)

    html = '<p class="mt-4">'
    for (const key in icons) {
      html += `<button class="btn btn-light btn-sm">${icons[key]}</button>`
    }

    document.querySelector('main').insertAdjacentHTML('beforeend', html)
    document.querySelector('main').insertAdjacentHTML('beforeend', '<br><br><div id="example-table"></div>')

    const response = await Helpers.fetchJSON(`${urlAPI}/client`)
    console.log(response)

    //initialize table
    let table = new Tabulator('#example-table', {
      data: response.data, //assign data to table
      autoColumns: true, //create columns from data field names
    })
  }
}
