define [
  'common/modals'
  'templates/issue'
], (
  ModalView
) ->

  class extends ModalView
    template: Templates['issue-rule']


    serializeData: ->
      _.extend super,
        permalink: "#{baseUrl}/coding_rules#rule_key=#{encodeURIComponent @model.get('key')}"
        allTags: _.union @model.get('sysTags'), @model.get('tags')
