<template>
  <nuxt-link :to="returnPath" style="color:inherit;text-decoration: inherit;">
    <b-card no-body class="full-height">
      <div class="aspect-box">
        <div
          :style="'background-image: url(' + getLogo + ')'"
          class="aspect-container"
        ></div>
      </div>

      <b-card-body>
        <b-card-title title-tag="div" class="h4">
          <span class="text-secondary">#</span>{{ entry.title }}
        </b-card-title>

        <b-card-text class="text-muted">
          {{ entry.descriptionAbstract }}
        </b-card-text>
      </b-card-body>

      <div slot="footer" class="row font-small">
        <div class="col-6">
          <div>
            Author:
            <span class="font-weight-bold">
              <span v-if="entry.authors.length > 0">{{
                entry.authors[0].name
              }}</span>
              <span v-else>unknown</span>
            </span>
            <span v-if="entry.authors.length > 1"> et al.</span>
          </div>
          <div>
            Category:
            <span class="font-weight-bold"
              ><span
                v-for="tag in entry.searchTags"
                :key="tag"
                class="text-capitalize implode"
                >{{ tag }}</span
              ></span
            >
          </div>
        </div>
        <div class="col-6">
          <div>
            Version:
            <span class="font-weight-bold">{{
              entry.currentVersion.versionTag
            }}</span>
          </div>
          <div>
            License:
            <span v-if="entry.license" class="font-weight-bold">{{
              entry.license.licenseId
            }}</span>
            <span v-else class="font-weight-bold">{{
              entry.licenseString
            }}</span>
          </div>
        </div>
      </div>
    </b-card>
  </nuxt-link>
</template>

<script>
export default {
  props: {
    entry: {
      default: null,
      type: Object
    },
    returnTo: {
      default: null,
      type: String
    }
  },
  computed: {
    getLogo() {
      if (this.entry.image) {
        return (
          this.$axios.defaults.baseURL +
          '/webcomponent/' +
          this.entry.uuid +
          '/logo'
        )
      }

      return '/component_placeholder.png'
    },
    returnPath() {
      if (this.returnTo === null) {
        return this.localePath({
          name: 'webcomponent-id',
          params: { id: this.entry.uuid }
        })
      }

      return this.localePath({
        name: 'webcomponent-id',
        params: { id: this.entry.uuid },
        query: { from: this.returnTo }
      })
    }
  }
}
</script>

<style lang="scss">
.aspect-box {
  height: 0;
  padding-top: 56.25%;
  position: relative;
}

.aspect-box .aspect-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;

  background-size: cover;

  /* img {
    object-fit: cover;
    max-width: 100%;
    height: 100%;
  } */
}
</style>
