<script setup>
import { onBeforeUnmount, shallowRef } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: '请输入内容',
  },
  height: {
    type: Number,
    default: 420,
  },
})

const emit = defineEmits(['update:modelValue'])

const editorRef = shallowRef()

const toolbarConfig = {}

const editorConfig = {
  placeholder: props.placeholder,
}

const handleCreated = (editor) => {
  editorRef.value = editor
}

const handleChange = (editor) => {
  emit('update:modelValue', editor.getHtml())
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
    editorRef.value = null
  }
})
</script>

<template>
  <div class="editor-shell">
    <Toolbar :editor="editorRef" :default-config="toolbarConfig" mode="default" />
    <Editor
      :model-value="modelValue"
      :default-config="editorConfig"
      mode="default"
      class="editor"
      :style="{ height: `${height}px` }"
      @on-created="handleCreated"
      @on-change="handleChange"
    />
  </div>
</template>

<style scoped>
.editor-shell {
  border: 1px solid var(--border);
  border-radius: 18px;
  overflow: hidden;
  background: #fff;
}

.editor {
  overflow-y: hidden;
}
</style>
