<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="name" prop="name">
      <el-input v-model="dataForm.name" placeholder="name"></el-input>
    </el-form-item>
    <el-form-item label="计费类型【0->按重量，1->按件数】" prop="chargeType">
      <el-input v-model="dataForm.chargeType" placeholder="计费类型【0->按重量，1->按件数】"></el-input>
    </el-form-item>
    <el-form-item label="首重" prop="firstWeight">
      <el-input v-model="dataForm.firstWeight" placeholder="首重"></el-input>
    </el-form-item>
    <el-form-item label="首费" prop="firstFee">
      <el-input v-model="dataForm.firstFee" placeholder="首费"></el-input>
    </el-form-item>
    <el-form-item label="续重" prop="continueWeight">
      <el-input v-model="dataForm.continueWeight" placeholder="续重"></el-input>
    </el-form-item>
    <el-form-item label="续费" prop="continueFee">
      <el-input v-model="dataForm.continueFee" placeholder="续费"></el-input>
    </el-form-item>
    <el-form-item label="目的地" prop="dest">
      <el-input v-model="dataForm.dest" placeholder="目的地"></el-input>
    </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
        dataForm: {
          id: 0,
          name: '',
          chargeType: '',
          firstWeight: '',
          firstFee: '',
          continueWeight: '',
          continueFee: '',
          dest: ''
        },
        dataRule: {
          name: [
            { required: true, message: 'name不能为空', trigger: 'blur' }
          ],
          chargeType: [
            { required: true, message: '计费类型【0->按重量，1->按件数】不能为空', trigger: 'blur' }
          ],
          firstWeight: [
            { required: true, message: '首重不能为空', trigger: 'blur' }
          ],
          firstFee: [
            { required: true, message: '首费不能为空', trigger: 'blur' }
          ],
          continueWeight: [
            { required: true, message: '续重不能为空', trigger: 'blur' }
          ],
          continueFee: [
            { required: true, message: '续费不能为空', trigger: 'blur' }
          ],
          dest: [
            { required: true, message: '目的地不能为空', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http({
              url: this.$http.adornUrl(`/wms/feighttemplate/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.name = data.feighttemplate.name
                this.dataForm.chargeType = data.feighttemplate.chargeType
                this.dataForm.firstWeight = data.feighttemplate.firstWeight
                this.dataForm.firstFee = data.feighttemplate.firstFee
                this.dataForm.continueWeight = data.feighttemplate.continueWeight
                this.dataForm.continueFee = data.feighttemplate.continueFee
                this.dataForm.dest = data.feighttemplate.dest
              }
            })
          }
        })
      },
      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/wms/feighttemplate/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'name': this.dataForm.name,
                'chargeType': this.dataForm.chargeType,
                'firstWeight': this.dataForm.firstWeight,
                'firstFee': this.dataForm.firstFee,
                'continueWeight': this.dataForm.continueWeight,
                'continueFee': this.dataForm.continueFee,
                'dest': this.dataForm.dest
              })
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: '操作成功',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      }
    }
  }
</script>
