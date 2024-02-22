# Configuration file for the Sphinx documentation builder.

# -- Project information

project = 'FA³ST Service'
copyright = '2022, Fraunhofer IOSB'
author = 'Fraunhofer IOSB'

# -- General configuration

extensions = [
    'sphinx.ext.duration',
    'sphinx.ext.doctest',
    'sphinx.ext.autodoc',
    'sphinx.ext.autosummary',
    'sphinx.ext.intersphinx',
    'myst_parser'
]

intersphinx_mapping = {
    'python': ('https://docs.python.org/3/', None),
    'sphinx': ('https://www.sphinx-doc.org/en/master/', None),
}
intersphinx_disabled_domains = ['std']

templates_path = ['_templates']

# -- Options for HTML output

html_theme = 'sphinx_rtd_theme'

html_theme_options = {	
	"navigation_depth": 2,
	"collapse_navigation": False,
	"logo_only": True
}

html_logo = 'images/logo-positiv.png'
html_show_sphinx = False
html_show_sourcelink  = False

myst_number_code_blocks = ["json"]

# -- Options for EPUB output
epub_show_urls = 'footnote'

# These folders are copied to the documentation's HTML output
html_static_path = ['_static']

# These paths are either relative to html_static_path
# or fully qualified paths (eg. https://...)
html_css_files = [
    'css/custom.css',
]

myst_enable_extensions = ["colon_fence"]