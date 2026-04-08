import { useState, useEffect } from 'react';
import { Table, Card, Button, Space, message, Tag, Tree, Modal, Form, Input, Select } from 'antd';
import type { TreeDataNode } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { getPermissionList, getPermissionTree } from '../../api/user';
import type { Permission } from '../../types';
import { formatDate } from '../../utils';

const PermissionList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<Permission[]>([]);
  const [treeData, setTreeData] = useState<TreeDataNode[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingPermission, setEditingPermission] = useState<Permission | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [listRes, treeRes] = await Promise.all([
        getPermissionList(),
        getPermissionTree(),
      ]);
      setDataSource(listRes.data);
      setTreeData(convertToTree(treeRes.data));
    } catch {
      message.error('获取权限列表失败');
    } finally {
      setLoading(false);
    }
  };

  const convertToTree = (permissions: Permission[]): TreeDataNode[] => {
    const map: Record<number, TreeDataNode> = {};
    const roots: TreeDataNode[] = [];

    permissions.forEach((p) => {
      map[p.id] = { key: p.id, title: `${p.name} (${p.code})`, children: [] };
    });

    permissions.forEach((p) => {
      if (p.parentId && map[p.parentId]) {
        map[p.parentId].children?.push(map[p.id]);
      } else {
        roots.push(map[p.id]);
      }
    });

    return roots;
  };

  const handleAdd = () => {
    setEditingPermission(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Permission) => {
    setEditingPermission(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = () => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该权限吗？',
      onOk: async () => {
        try {
          message.success('删除成功');
          fetchData();
        } catch {
          message.error('删除失败');
        }
      },
    });
  };

  const handleSubmit = async () => {
    try {
      await form.validateFields();
      message.success(editingPermission ? '更新成功' : '创建成功');
      setModalVisible(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const getPermissionTypeText = (type: number): string => {
    const typeMap: Record<number, string> = {
      0: '目录',
      1: '菜单',
      2: '按钮',
    };
    return typeMap[type] || '未知';
  };

  const columns: ColumnsType<Permission> = [
    { title: '权限名称', dataIndex: 'name', key: 'name' },
    { title: '权限编码', dataIndex: 'code', key: 'code' },
    {
      title: '权限类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: number) => {
        const colorMap: Record<number, string> = { 0: 'purple', 1: 'blue', 2: 'green' };
        return <Tag color={colorMap[type]}>{getPermissionTypeText(type)}</Tag>;
      },
    },
    { title: '路径', dataIndex: 'path', key: 'path' },
    { title: '图标', dataIndex: 'icon', key: 'icon' },
    { title: '排序', dataIndex: 'sort', key: 'sort' },
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
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', render: (text) => formatDate(text) },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete()}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Card
      title="权限管理"
      extra={
        <Space>
          <Button icon={<ReloadOutlined />} onClick={fetchData}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增权限
          </Button>
        </Space>
      }
    >
      <div style={{ marginBottom: 16 }}>
        <h4>权限树</h4>
        <Tree treeData={treeData} defaultExpandAll />
      </div>

      <Table
        columns={columns}
        dataSource={dataSource}
        rowKey="id"
        loading={loading}
        pagination={false}
      />

      <Modal
        title={editingPermission ? '编辑权限' : '新增权限'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={500}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="权限名称" rules={[{ required: true, message: '请输入权限名称' }]}>
            <Input placeholder="请输入权限名称" />
          </Form.Item>
          <Form.Item name="code" label="权限编码" rules={[{ required: true, message: '请输入权限编码' }]}>
            <Input placeholder="请输入权限编码" />
          </Form.Item>
          <Form.Item name="type" label="权限类型" initialValue={2}>
            <Select>
              <Select.Option value={0}>目录</Select.Option>
              <Select.Option value={1}>菜单</Select.Option>
              <Select.Option value={2}>按钮</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="path" label="路径">
            <Input placeholder="请输入路径" />
          </Form.Item>
          <Form.Item name="icon" label="图标">
            <Input placeholder="请输入图标" />
          </Form.Item>
          <Form.Item name="sort" label="排序" initialValue={0}>
            <Input type="number" placeholder="请输入排序号" />
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

export default PermissionList;
