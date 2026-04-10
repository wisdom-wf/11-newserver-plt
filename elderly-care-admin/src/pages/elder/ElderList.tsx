import { useState, useEffect } from 'react';
import { Table, Card, Button, Space, message, Tag, Modal, Form, Input, Select } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, CheckOutlined, CloseOutlined, DownloadOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { getElderList, createElder, updateElder, deleteElder, enableElder, disableElder, exportElders } from '../../api/elder';
import type { Elder, ElderQuery } from '../../types';
import { getGenderText, getCareLevelText } from '../../utils';

const ElderList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<Elder[]>([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingElder, setEditingElder] = useState<Elder | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async (page = 1, pageSize = 10) => {
    try {
      setLoading(true);
      const response = await getElderList({ page, pageSize } as ElderQuery);
      setDataSource(response.data.list);
      setPagination({
        current: response.data.page,
        pageSize: response.data.pageSize,
        total: response.data.total,
      });
    } catch {
      message.error('获取老人列表失败');
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = (pag: TablePaginationConfig) => {
    if (pag.current && pag.pageSize) {
      fetchData(pag.current, pag.pageSize);
    }
  };

  const handleAdd = () => {
    setEditingElder(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Elder) => {
    setEditingElder(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = (id: string) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该老人吗？',
      onOk: async () => {
        try {
          await deleteElder(id);
          message.success('删除成功');
          fetchData(pagination.current, pagination.pageSize);
        } catch {
          message.error('删除失败');
        }
      },
    });
  };

  const handleToggleStatus = async (id: string, currentStatus: number) => {
    try {
      if (currentStatus === 1) {
        await disableElder(id);
      } else {
        await enableElder(id);
      }
      message.success('状态更新成功');
      fetchData(pagination.current, pagination.pageSize);
    } catch {
      message.error('状态更新失败');
    }
  };

  const handleExport = async () => {
    try {
      const response = await exportElders();
      const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'elder_list.xls';
      link.click();
      window.URL.revokeObjectURL(url);
      message.success('导出成功');
    } catch {
      message.error('导出失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingElder) {
        await updateElder(editingElder.id, values);
        message.success('更新成功');
      } else {
        await createElder(values);
        message.success('创建成功');
      }
      setModalVisible(false);
      fetchData(pagination.current, pagination.pageSize);
    } catch {
      message.error('操作失败');
    }
  };

  const columns: ColumnsType<Elder> = [
    { title: '姓名', dataIndex: 'name', key: 'name' },
    {
      title: '性别',
      dataIndex: 'gender',
      key: 'gender',
      render: (gender: number) => getGenderText(gender),
    },
    { title: '年龄', dataIndex: 'age', key: 'age' },
    { title: '联系电话', dataIndex: 'phone', key: 'phone' },
    { title: '地址', dataIndex: 'address', key: 'address', ellipsis: true },
    {
      title: '护理级别',
      dataIndex: 'careLevel',
      key: 'careLevel',
      render: (level: number) => getCareLevelText(level),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            icon={record.status === 1 ? <CloseOutlined /> : <CheckOutlined />}
            onClick={() => handleToggleStatus(record.id, record.status)}
          >
            {record.status === 1 ? '禁用' : '启用'}
          </Button>
          <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Card
      title="老人客户管理"
      extra={
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => fetchData(pagination.current, pagination.pageSize)}>
            刷新
          </Button>
          <Button icon={<DownloadOutlined />} onClick={handleExport}>
            导出
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增老人
          </Button>
        </Space>
      }
    >
      <Table
        columns={columns}
        dataSource={dataSource}
        rowKey="id"
        loading={loading}
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
        }}
        onChange={handleTableChange}
      />

      <Modal
        title={editingElder ? '编辑老人' : '新增老人'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="姓名" rules={[{ required: true, message: '请输入姓名' }]}>
            <Input placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item name="idCard" label="身份证号" rules={[{ required: true, message: '请输入身份证号' }]}>
            <Input placeholder="请输入身份证号" />
          </Form.Item>
          <Form.Item name="phone" label="联系电话" rules={[{ required: true, message: '请输入联系电话' }]}>
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item name="address" label="地址">
            <Input.TextArea placeholder="请输入地址" rows={2} />
          </Form.Item>
          <Form.Item name="careLevel" label="护理级别" initialValue={1}>
            <Select>
              <Select.Option value={1}>能力完好</Select.Option>
              <Select.Option value={2}>轻度失能</Select.Option>
              <Select.Option value={3}>中度失能</Select.Option>
              <Select.Option value={4}>重度失能</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="status" label="状态" initialValue={1}>
            <Select>
              <Select.Option value={1}>启用</Select.Option>
              <Select.Option value={0}>禁用</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default ElderList;
