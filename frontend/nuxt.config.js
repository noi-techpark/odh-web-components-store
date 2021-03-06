module.exports = {
  mode: 'spa',

  srcDir: 'src/',

  /*
   ** Headers of the page
   */
  head: {
    title: 'ODH Webcomponents',
    meta: [
      { charset: 'utf-8' },
      { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      {
        hid: 'description',
        name: 'description',
        content: 'ODH Webcomponents'
      }
    ],
    link: [{ rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }]
  },
  /*
   ** Customize the progress-bar color
   */
  loading: { color: '#fff' },
  /*
   ** Global CSS
   */
  css: [
    '@/assets/styles/styles.scss',
    'vue-loading-overlay/dist/vue-loading.css',
    '@fortawesome/fontawesome-svg-core/styles.css'
  ],

  bootstrapVue: {
    bootstrapCSS: false,
    bootstrapVueCSS: false
  },

  /*
   ** Plugins to load before mounting the App
   */
  plugins: [
    '~/plugins/api.js',
    '~/plugins/fontawesome.js',
    '~/plugins/tooltip.js'
  ],
  /*
   ** Nuxt.js dev-modules
   */
  buildModules: [
    // Doc: https://github.com/nuxt-community/eslint-module
    '@nuxtjs/eslint-module',
    // Doc: https://github.com/nuxt-community/analytics-module
    [
      '@nuxtjs/google-analytics',
      {
        id: process.env.GOOGLE_ANALYTICS_ID,
        debug: {
          enabled: process.env.GOOGLE_ANALYTICS_DEBUG || false,
          sendHitTask: process.env.GOOGLE_ANALYTICS_DEBUG || false
        }
      }
    ]
  ],
  /*
   ** Nuxt.js modules
   */
  modules: [
    // Doc: https://bootstrap-vue.js.org/docs/
    'bootstrap-vue/nuxt',
    // Doc: https://axios.nuxtjs.org/usage
    '@nuxtjs/axios',
    'nuxt-i18n',
    ['vue-scrollto/nuxt', { duration: 500 }]
  ],

  i18n: {
    locales: [
      {
        code: 'en',
        file: 'en-US.js'
      }
    ],
    defaultLocale: 'en',
    lazy: true,
    langDir: 'assets/locales/',
    detectBrowserLanguage: {
      useCookie: true,
      cookieKey: 'i18n_redirected'
    },
    strategy: 'prefix_except_default'
  },

  /*
   * Needed to access it inside templates to generate static links
   */
  env: {
    API_LOCATION: process.env.API_BASE_URL || 'http://localhost:9030'
  },

  /*
   ** Axios module configuration
   ** See https://axios.nuxtjs.org/options
   */
  axios: {
    baseURL: process.env.API_BASE_URL || 'http://localhost:9030'
  },

  /*
   ** Build configuration
   */
  build: {
    /*
     ** You can extend webpack config here
     */
    extend(config, ctx) {
      // Run ESLint on save
      if (ctx.isDev) {
        if (ctx.isClient) {
          config.module.rules.push({
            enforce: 'pre',
            test: /\.(js|ts|vue)$/,
            loader: 'eslint-loader',
            exclude: /(node_modules)/,
            options: {
              fix: true
            }
          })
          config.devtool = 'source-map'
        } else {
          config.devtool = 'inline-source-map'
        }
      }
    }
  }
}
