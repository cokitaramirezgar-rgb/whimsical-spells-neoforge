Whimsical Spells - addon para Iron's Spells 'n Spellbooks (NeoForge 1.21.1)

COMPILAR SIN INSTALAR NADA (GitHub Actions)
1. Crea un repositorio nuevo en github.com y sube TODO el contenido de esta carpeta
   (incluida la carpeta oculta .github; si no se arrastra, crea .github/workflows/build.yml a mano
   con Add file > Create new file y pega el contenido de build.yml).
2. Pestaña "Actions": el workflow "Build mod" arranca solo.
3. Cuando salga el check verde, abre la ejecución y descarga el artefacto "whimsical_spells-jar".
4. Descomprime y mete el .jar en la carpeta mods junto con Iron's Spells 'n Spellbooks 1.21.1.

PROBAR LOS HECHIZOS
En 1.21.1 el formato de los pergaminos cambió (componentes de datos en vez de NBT), así que el
comando /give de la versión 1.20.1 ya no sirve. Lo más fácil: en modo creativo, busca los
pergaminos en la pestaña de Iron's Spells 'n Spellbooks (suele haber uno por hechizo, también
los de addons) o usa la mesa de inscripción.
Ids de los hechizos: whimsical_spells:flower_field, repair_item, magic_trace, jubelade,
medrozobalt, mana_suppression, reamstroha y meehkeit.
