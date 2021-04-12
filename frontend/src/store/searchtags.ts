import { Action, Module, Mutation, VuexModule } from 'vuex-module-decorators';
import { $api } from '~/utils/api-accessor';

@Module({
  name: 'searchtags',
  stateFactory: true,
  namespaced: true,
})
export default class SearchtagsModule extends VuexModule {
  searchtags: Array<string> = [];

  get getSearchtags(): Array<string> {
    return this.searchtags;
  }

  @Mutation
  setSearchtags(searchtags: Array<string>) {
    this.searchtags = searchtags;
  }

  @Action
  async loadSearchtags() {
    const result = await $api.searchtag.listAll();

    this.setSearchtags(result);
  }
}