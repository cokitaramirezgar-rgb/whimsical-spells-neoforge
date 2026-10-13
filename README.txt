# Whimsical Spells

Un complemento para **Iron's Spells 'n Spellbooks** (NeoForge 1.21.1) que añade 8 hechizos nuevos al juego.

## Requisitos

- Minecraft 1.21.1
- NeoForge (versión compatible con el mod)
- **Iron's Spells 'n Spellbooks** (dependencia obligatoria)

## Hechizos incluidos

| ID del hechizo | Nombre | Descripción |
|---|---|---|
| `flower_field` | Campo de flores | Cubre el suelo a tu alrededor con flores silvestres. Ocasionalmente hace brotar un girasol. |
| `repair_item` | Reparar objeto | Repara el objeto dañado que sostienes en la mano usando maná. Lánzalo agachado para reparar armaduras desgastadas. |
| `magic_trace` | Rastro de poder mágico | Detecta estructuras mágicas cercanas y deja un rastro de partículas que apunta a la más cercana. |
| `jubelade` | Jubelade: Pétalos de acero | Convierte pétalos de flores en acero y los dispara en línea recta, dañando todo a su paso. |
| `medrozobalt` | Medrozobalt: Torbellino | Convierte tu ataque en un torbellino que empuja y daña a los enemigos circundantes. |
| `mana_suppression` | Supresión de maná | Oculta tu presencia mágica, haciendo que las criaturas hostiles te detecten desde una distancia mucho menor. |
| `reamstroha` | Reamstroha: Manipulación de agua | Reúne agua en una esfera sobre el objetivo y la deja caer como una cascada. Lánzalo agachado para curar a aliados en su lugar. |
| `meehkeit` | Meehkeit: Detección de cofres trampa | Detecta cofres trampa cercanos (incluso los ocultos) y los marca con columnas de llamas azules. |
| `reelseiden` | *[Información pendiente]* | *[Detalles del hechizo por añadir]* |

## Instalación

1. Asegúrate de tener **NeoForge** instalado para Minecraft 1.21.1. 2. Descarga **Iron's Spells 'n Spellbooks** y colócalo en tu carpeta `mods`.
3. Descarga el archivo `.jar` de **Whimsical Spells**.
4. Coloca el archivo `.jar` en tu carpeta `mods`.
5. Inicia el juego.

## Cómo obtener los hechizos

En la versión 1.21.1, los pergaminos utilizan componentes de datos (*data components*) en lugar de NBT, por lo que los antiguos comandos `/give` de la versión 1.20.1 ya no funcionan. Para probar los hechizos:

- **Modo Creativo**: Busca los pergaminos en la pestaña del modo creativo de **Iron's Spells 'n Spellbooks** (normalmente hay un pergamino por hechizo, incluyendo los de los complementos).
- **Modo Supervivencia**: Utiliza la **Mesa de Inscripción** (*Inscription Table*) de Iron's Spells 'n Spellbooks.

IDs de los hechizos (para comandos o *datapacks*):
`whimsical_spells:flower_field`, `whimsical_spells:repair_item`, `whimsical_spells:magic_trace`, `whimsical_spells:jubelade`, `whimsical_spells:medrozobalt`, `whimsical_spells:mana_suppression`, `whimsical_spells:reamstroha`, `whimsical_spells:meehkeit`

## Compilación desde el código fuente (GitHub Actions)

Puedes compilar el mod sin instalar nada localmente utilizando GitHub Actions:

1. Crea un nuevo repositorio en GitHub y sube **todo el contenido** de esta carpeta, incluida la carpeta oculta `.github`. Si no se sube correctamente, crea manualmente el archivo `.github/workflows/build.yml` con el contenido del archivo `build.yml` proporcionado.
2. Ve a la pestaña **Actions**. El flujo de trabajo **"Build mod"** se iniciará automáticamente.
3. Cuando aparezca la marca de verificación verde, abre la ejecución y descarga el artefacto **`whimsical_spells-jar`**. 4. Descomprime el archivo y coloca el `.jar` en tu carpeta `mods` junto con **Iron's Spells 'n Spellbooks 1.21.1**.

## Créditos

- **Autor del mod**: cokitaramirezgar-rgb
- **Dependencia**: Iron's Spells 'n Spellbooks, de Iron431

## Licencia

*[Especifica aquí tu licencia; p. ej., MIT, GPL, Todos los derechos reservados]*
